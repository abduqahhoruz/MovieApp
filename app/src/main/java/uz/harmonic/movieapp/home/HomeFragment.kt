package uz.harmonic.movieapp.home

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import com.liulishuo.filedownloader.BaseDownloadTask
import com.liulishuo.filedownloader.FileDownloadQueueSet
import com.liulishuo.filedownloader.FileDownloader
import com.liulishuo.filedownloader.notification.BaseNotificationItem
import com.liulishuo.filedownloader.notification.FileDownloadNotificationListener
import dagger.hilt.android.AndroidEntryPoint
import uz.harmonic.movieapp.MainActivity
import uz.harmonic.movieapp.R
import uz.harmonic.movieapp.common.Constants
import uz.harmonic.movieapp.common.lazyFast
import uz.harmonic.movieapp.data.DownloadStatus
import uz.harmonic.movieapp.data.MP4Payloads
import uz.harmonic.movieapp.data.Pojo
import uz.harmonic.movieapp.databinding.AddDialogLayoutBinding
import uz.harmonic.movieapp.databinding.FragmentHomeBinding
import uz.harmonic.movieapp.home.notification.NotificationItem
import uz.harmonic.movieapp.home.notification.NotificationUtils
import uz.harmonic.movieapp.util.NetworkStatus

@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home), IOnItemClickListener, View.OnClickListener {
    private val binding: FragmentHomeBinding by viewBinding()
    private var _alertDialogBinding: AddDialogLayoutBinding? = null
    private val alertDialogBinding get() = _alertDialogBinding!!
    private val mViewModel: HomeViewModel by viewModels()
    private val list: MutableList<Pojo> = mutableListOf()
    private lateinit var pojo: Pojo
    private val fileDownloader by lazy(LazyThreadSafetyMode.NONE) { FileDownloader.getImpl() }
    private val filterDM by lazyFast { requireActivity().cacheDir.absolutePath }
    private lateinit var mAdapter: MP4Adapter
    private val observerError = Observer<String> {
        if (it == "") {
            _alertDialogBinding = null
            alertDialog?.hide()
        } else {
            Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            alertDialog?.hide()
        }

    }
    private val observerLoadPojoStatus = Observer<NetworkStatus> {
        when (it) {
            is NetworkStatus.LOADING -> {
                binding.pbLoading.isVisible = true
                binding.rvPojo.isVisible = false
                binding.llcError.isVisible = false
            }
            is NetworkStatus.ERROR -> {
                Toast.makeText(
                    requireContext(),
                    requireContext().getString(it.res),
                    Toast.LENGTH_SHORT
                )
                    .show()
                binding.pbLoading.isVisible = false
                binding.rvPojo.isVisible = false
                binding.llcError.isVisible = true
            }
            else -> {
                binding.pbLoading.isVisible = false
                binding.rvPojo.isVisible = true
                binding.llcError.isVisible = false
            }
        }
    }
    private val observerList = Observer<List<Pojo>> {
        list.clear()
        Log.d("OnListObserver", ": ${it.joinToString()}")
        list.addAll(it)
        mAdapter.submitList(it)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        NotificationUtils.createNotificationChannel(
            Constants.CHANNEL_ID,
            "MyFileDownloader",
            requireActivity().applicationContext
        )
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        setupObserver()
    }

    private fun setupObserver() {
        mViewModel.liveError.observe(viewLifecycleOwner, observerError)
        mViewModel.liveLoadPojoStatus.observe(viewLifecycleOwner, observerLoadPojoStatus)
        mViewModel.livePojoList.observe(viewLifecycleOwner, observerList)
    }

    private fun setupViews() {
        mAdapter = MP4Adapter(list, this, listOf())
        binding.rvPojo.adapter = mAdapter
        binding.btnRetry.setOnClickListener(this)
        binding.fabAdd.setOnClickListener {
            alertDialog?.show()
            alertDialogBinding.actionStartDownload.setOnClickListener {
                mViewModel.addUrl(alertDialogBinding.inputUrl.text.toString())
            }
        }
    }

    override fun onClickDownload(adapterPosition: Int, pojo: Pojo) {
        this.pojo = pojo
        val queueSet = FileDownloadQueueSet(listener(adapterPosition))
        val task = fileDownloader.create(pojo.url)
        task.setPath("$filterDM/${pojo.fileName}.mp4", false)

        queueSet.downloadTogether(task)
            .addTaskFinishListener {
                (task.listener as FileDownloadNotificationListener).destroyNotification(task)
            }
            .start()

    }

    override fun onClickInfo(position: Int, fileName: String, title: String, url: String) {
        val args = bundleOf(
            Pair("key_title", title),
            Pair("key_fileName", fileName),
            Pair("key_description", url)
        )
        findNavController().navigate(R.id.navigation_home_info, args)
    }

    override fun onClickPlay(position: Int, pojo: Pojo) {
        onClickDownload(position, pojo)
    }

    override fun onClickPause(id: Int) {
        fileDownloader.pause(id)
    }

    override fun onClickCancel(pos: Int, id: Int, fileName: String) {
        if (fileDownloader.clear(id, fileName)) {
            mAdapter.setStatus(pos, DownloadStatus.CANCEL, MP4Payloads.FILESTATUS)
            mViewModel.deleteVideo(list[pos].id)
        }
    }

    private fun onRetry() {

    }

    private fun listener(position: Int): FileDownloadNotificationListener {
        return object :
            FileDownloadNotificationListener((requireActivity() as MainActivity).notificationHelper) {

            override fun create(task: BaseDownloadTask?): BaseNotificationItem {
                return NotificationItem(
                    task!!.id,
                    "Task ${task.filename.split('.')[0]}",
                    "aa",
                    Constants.CHANNEL_ID
                )
            }

            override fun interceptCancel(
                task: BaseDownloadTask?,
                notificationItem: BaseNotificationItem?
            ): Boolean {
                return true
            }

            override fun connected(
                task: BaseDownloadTask?,
                etag: String?,
                isContinue: Boolean,
                soFarBytes: Int,
                totalBytes: Int
            ) {
                super.connected(task, etag, isContinue, soFarBytes, totalBytes)
                mAdapter.setStatus(position, DownloadStatus.CONNECTED, MP4Payloads.FILESTATUS)
                mViewModel.updateVideoStatus(DownloadStatus.CONNECTED, list[position].id)

            }

            override fun progress(
                task: BaseDownloadTask?,
                soFarBytes: Int,
                totalBytes: Int
            ) {
                super.progress(task, soFarBytes, totalBytes)
                //change status by viewModel
                //change downloaded bytes by viewModel
                mAdapter.setStatus(position, soFarBytes, totalBytes, MP4Payloads.FILEDOWNLOADING)
                mViewModel.updateBytes(0, soFarBytes, totalBytes)
            }

            override fun completed(task: BaseDownloadTask?) {
                super.completed(task)
                mAdapter.setStatus(
                    position,
                    task?.largeFileSoFarBytes?.toInt() ?: 0,
                    task?.largeFileTotalBytes?.toInt() ?: 0,
                    MP4Payloads.FILESTATUS
                )
                mViewModel.updateVideoStatus(DownloadStatus.SUCCESS, pojo.id)
                mViewModel.updateBytes(
                    list[position].id,
                    soFarBytes = task?.largeFileSoFarBytes?.toInt() ?: 0,
                    task?.largeFileTotalBytes?.toInt() ?: 0
                )
                mAdapter.setStatus(position, DownloadStatus.SUCCESS, MP4Payloads.FILESTATUS)

            }

            override fun paused(
                task: BaseDownloadTask?,
                soFarBytes: Int,
                totalBytes: Int
            ) {
                super.paused(task, soFarBytes, totalBytes)
                mViewModel.updateVideoStatus(DownloadStatus.PAUSED, list[position].id)

            }

            override fun error(task: BaseDownloadTask?, e: Throwable?) {
                super.error(task, e)
                mViewModel.updateVideoStatus(DownloadStatus.ERROR, list[position].id)
                mAdapter.setStatus(position, DownloadStatus.ERROR, MP4Payloads.FILESTATUS)
            }

        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            binding.btnRetry.id -> {
                onRetry()
            }
            else -> {
            }
        }
    }

    private val alertDialog by lazy {
        _alertDialogBinding = AddDialogLayoutBinding.inflate(layoutInflater)
        context?.let {
            androidx.appcompat.app.AlertDialog.Builder(it)
                .setView(alertDialogBinding.root)
                .setCancelable(false)
                .create()
        }
    }


}
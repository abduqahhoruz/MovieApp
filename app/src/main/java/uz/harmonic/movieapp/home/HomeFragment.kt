package uz.harmonic.movieapp.home

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
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
import java.io.File

@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home), IOnItemClickListener {
    private val binding: FragmentHomeBinding by viewBinding()
    private val mViewModel: HomeViewModel by viewModels()
    private val list: MutableList<Pojo> = mutableListOf()
    private val fileDownloader by lazy(LazyThreadSafetyMode.NONE) { FileDownloader.getImpl() }
    private val filterDM by lazyFast { requireActivity().cacheDir.absolutePath }
    private lateinit var mAdapter: MP4Adapter
    private val observerError = Observer<String> {
        if (it == "") {
            alertDialog?.hide()
        } else {
            Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            alertDialog?.hide()
        }

    }
    private val observerList = Observer<List<Pojo>> {
        if (it.isNotEmpty()) {
            binding.tvError.visibility = View.GONE
            list.clear()
            list.addAll(it)
            mAdapter.submitList(it)
        } else {
            binding.tvError.visibility = View.VISIBLE
        }

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
        mViewModel.livePojoList.observe(viewLifecycleOwner, observerList)
    }

    private fun setupViews() {
        mAdapter = MP4Adapter(list, this, listOf())
        binding.rvPojo.adapter = mAdapter
        binding.fabAdd.setOnClickListener {
            alertDialog?.show()
        }
    }

    override fun onClickDownload(position: Int) {
        if (list[position].status == DownloadStatus.SUCCESS) {
            onClickPlay(position)
        } else {
            val queueSet = FileDownloadQueueSet(listener(position))
            val task = fileDownloader.create(list[position].url)
            task.setPath("$filterDM/${list[position].fileName}", false)
            queueSet.downloadTogether(task)
                .addTaskFinishListener {
                    (task.listener as FileDownloadNotificationListener).destroyNotification(task)
                }
                .start()
        }

    }


    override fun onClickPlay(position: Int) {
        val args = bundleOf(
            Pair("key_title", list[position].title),
            Pair("key_fileName", list[position].fileName),
            Pair("key_description", list[position].url)
        )
        findNavController().navigate(R.id.navigation_home_info, args)
    }

    override fun onClickPause(position: Int) {
        fileDownloader.pause(list[position].taskId)
    }

    override fun onClickCancel(position: Int) {
        try {
            val boolean = fileDownloader.clear(id, list[position].fileName)
            if (boolean) {
                list[position].status = DownloadStatus.EMPTY
                mAdapter.notifyItemChanged(position, MP4Payloads.FILESTATUS)
                mViewModel.update(list[position])
            } else {

            }
        } catch (e: Exception) {

        }
    }

    override fun onClickDelete(position: Int) {
        try {
            val file = File("$filterDM/${list[position].fileName}")
            if (file.delete()) {
                fileDownloader.clear(id, list[position].fileName)
                mViewModel.deleteVideo(list[position].id)
            } else {
                Toast.makeText(requireContext(), "Not deleted", Toast.LENGTH_SHORT).show()
            }


        } catch (e: Exception) {
            Toast.makeText(requireContext(), "${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun listener(position: Int): FileDownloadNotificationListener {
        return object :
            FileDownloadNotificationListener((requireActivity() as MainActivity).notificationHelper) {

            override fun create(task: BaseDownloadTask?): BaseNotificationItem {
                list[position].taskId = task?.id ?: -1
                mViewModel.update(list[position])
                return NotificationItem(
                    task!!.id,
                    "Task ${task.filename.split('.')[0]}",
                    "aa",
                    Constants.CHANNEL_ID
                )
            }

            override fun connected(
                task: BaseDownloadTask?,
                etag: String?,
                isContinue: Boolean,
                soFarBytes: Int,
                totalBytes: Int
            ) {
                super.connected(task, etag, isContinue, soFarBytes, totalBytes)
                list[position].status = DownloadStatus.CONNECTED
                mAdapter.notifyItemChanged(position, MP4Payloads.FILESTATUS)
                mViewModel.update(list[position])
            }

            override fun progress(
                task: BaseDownloadTask?,
                soFarBytes: Int,
                totalBytes: Int
            ) {
                super.progress(task, soFarBytes, totalBytes)
                list[position].soFarBytes = soFarBytes
                list[position].totalBytes = totalBytes
                list[position].taskId = task?.id ?: -1
                mAdapter.notifyItemChanged(position, MP4Payloads.FILEDOWNLOADING)
                mViewModel.update(list[position])
            }

            override fun completed(task: BaseDownloadTask?) {
                super.completed(task)
                list[position].soFarBytes = task?.largeFileSoFarBytes?.toInt() ?: 0
                list[position].totalBytes = task?.largeFileTotalBytes?.toInt() ?: 0
                list[position].status = DownloadStatus.SUCCESS
                mAdapter.notifyItemChanged(position, MP4Payloads.FILESTATUS)
                mViewModel.update(list[position])
            }

            override fun error(task: BaseDownloadTask?, e: Throwable?) {
                super.error(task, e)
                list[position].status = DownloadStatus.ERROR
                mAdapter.notifyItemChanged(position, MP4Payloads.FILESTATUS)
                mViewModel.update(list[position])
            }

        }
    }


    private val alertDialog by lazy {
        val alertDialogBinding = AddDialogLayoutBinding.inflate(layoutInflater)
        alertDialogBinding.actionStartDownload.setOnClickListener {
            mViewModel.addUrl(alertDialogBinding.inputUrl.text.toString())
            alertDialogBinding.inputUrl.setText("")
        }
        context?.let {
            AlertDialog.Builder(it)
                .setView(alertDialogBinding.root)
                .setCancelable(true)
                .create()
        }
    }
}
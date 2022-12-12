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
import timber.log.Timber
import uz.harmonic.movieapp.MainActivity
import uz.harmonic.movieapp.R
import uz.harmonic.movieapp.common.Constants
import uz.harmonic.movieapp.common.lazyFast
import uz.harmonic.movieapp.data.DownloadStatus
import uz.harmonic.movieapp.data.MP4Payloads
import uz.harmonic.movieapp.data.Pojo
import uz.harmonic.movieapp.databinding.FragmentHomeBinding
import uz.harmonic.movieapp.home.notification.NotificationItem
import uz.harmonic.movieapp.home.notification.NotificationUtils
import uz.harmonic.movieapp.util.NetworkStatus

class HomeFragment : Fragment(R.layout.fragment_home), IOnItemClickListener, View.OnClickListener {
    private val binding: FragmentHomeBinding by viewBinding()
    private val mViewModel: HomeViewModel by viewModels()
    private val list: MutableList<Pojo> = mutableListOf()
    private val fileDownloader by lazy(LazyThreadSafetyMode.NONE) { FileDownloader.getImpl() }
    private val filterDM by lazyFast { requireActivity().cacheDir.absolutePath }
    private lateinit var mAdapter: MP4Adapter
    private val observerLoadPojoStatus = Observer<NetworkStatus> {
        when (it) {
            is NetworkStatus.LOADING -> {
                binding.pbLoading.isVisible = true
                binding.rvPojo.isVisible = false
                binding.llcError.isVisible = false
            }
            is NetworkStatus.ERROR -> {
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
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mViewModel.loadPojoList()
        NotificationUtils.createNotificationChannel(
            Constants.CHANNEL_ID,
            "MyFileDownloader",
            requireActivity().applicationContext
        )
    }

    private fun getFileNames(): List<String> {
        val finalList = mutableListOf<String>()
        val homeFolder = requireActivity().cacheDir
        val fileList = homeFolder?.listFiles()
        Timber.tag("TAGTAG").d("getFileNames:" + fileList?.joinToString() + " ")
        if (fileList != null) {
            for (file in fileList) {
                finalList.add(file.name)
            }
        } else {
            Toast.makeText(requireActivity(), "$fileList, Ishlamavotti", Toast.LENGTH_SHORT).show()
        }

        return finalList
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        setupObserver()
    }

    private fun setupObserver() {
        mViewModel.liveLoadPojoStatus.observe(viewLifecycleOwner, observerLoadPojoStatus)
        mViewModel.livePojoList.observe(viewLifecycleOwner, observerList)
    }

    private fun setupViews() {
        Timber.tag("TAG").d("setupViews: %s", getFileNames())
        mAdapter = MP4Adapter(list, this, getFileNames())
        binding.rvPojo.adapter = mAdapter
        binding.btnRetry.setOnClickListener(this)
    }

    override fun onClickDownload(position: Int, fileName: String, url: String): Int {
        val id: Int
        val queueSet = FileDownloadQueueSet(listener(position))
        val task = fileDownloader.create(url)
        task.setPath("$filterDM/$fileName.mp4", false)
        id = task.id

        queueSet.downloadTogether(task)
            .addTaskFinishListener {
                (task.listener as FileDownloadNotificationListener).destroyNotification(task)
            }
            .start()

        return id
    }

    override fun onClickInfo(position: Int, fileName: String, title: String, description: String) {
        val args = bundleOf(
            Pair("key_title", title),
            Pair("key_fileName", fileName),
            Pair("key_description", description)
        )
        findNavController().navigate(R.id.navigation_home_info, args)
    }

    override fun onClickPlay(position: Int, fileName: String, url: String) {
        onClickDownload(position, fileName, url)
    }

    override fun onClickPause(id: Int) {
        fileDownloader.pause(id)
    }

    override fun onClickCancel(position: Int, id: Int, fileName: String) {
        if (fileDownloader.clear(id, fileName)) {
            mAdapter.setStatus(position, DownloadStatus.CANCEL, MP4Payloads.FILESTATUS)
        }
    }

    private fun onRetry() {
        mViewModel.loadPojoList()
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
            }

            override fun progress(
                task: BaseDownloadTask?,
                soFarBytes: Int,
                totalBytes: Int
            ) {
                super.progress(task, soFarBytes, totalBytes)
                mAdapter.setStatus(position, soFarBytes, totalBytes, MP4Payloads.FILEDOWNLOADING)
            }

            override fun completed(task: BaseDownloadTask?) {
                super.completed(task)
                mAdapter.setStatus(
                    position,
                    task?.largeFileSoFarBytes?.toInt() ?: 0,
                    task?.largeFileTotalBytes?.toInt() ?: 0,
                    MP4Payloads.FILESTATUS
                )
                mAdapter.setStatus(position, DownloadStatus.SUCCESS, MP4Payloads.FILESTATUS)

            }

            override fun paused(
                task: BaseDownloadTask?,
                soFarBytes: Int,
                totalBytes: Int
            ) {
                super.paused(task, soFarBytes, totalBytes)
                mAdapter.setStatus(position, DownloadStatus.PAUSED, MP4Payloads.FILESTATUS)
            }

            override fun error(task: BaseDownloadTask?, e: Throwable?) {
                super.error(task, e)
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

    fun getDownloadedFileNames() {

    }

}
package uz.harmonic.movieapp.home

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import timber.log.Timber
import uz.harmonic.movieapp.MyApp
import uz.harmonic.movieapp.R
import uz.harmonic.movieapp.common.Constants
import uz.harmonic.movieapp.data.DownloadStatus
import uz.harmonic.movieapp.data.MP4Payloads
import uz.harmonic.movieapp.data.Pojo
import uz.harmonic.movieapp.data.prefs.PrefHelper
import uz.harmonic.movieapp.data.prefs.Prefs

import uz.harmonic.movieapp.databinding.ItemRowMp4Binding
import java.math.BigDecimal

class MP4Adapter(
    private val mDatalist: MutableList<Pojo>,
    private val onClickListener: IOnItemClickListener,
    private val fileNames: List<String>
) : RecyclerView.Adapter<MP4Adapter.MP4VH>() {

    class MP4VH(
        private val binding: ItemRowMp4Binding,
        private val onClickListener: IOnItemClickListener,
        private val fileNames: List<String>,
        private val mDatalist: MutableList<Pojo>
    ) : RecyclerView.ViewHolder(binding.root), View.OnClickListener {
        private var statusText: String = "status"

        fun onBind(pojo: Pojo) {
            with(binding) {
                Timber.tag("WTFTAG").d("POJO: " + pojo + "  filenames: " + fileNames)
                Glide.with(root.context)
                    .load("${Constants.BASE_URL}${pojo.thumb}")
                    .placeholder(R.drawable.ic_video_placeholder)
                    .into(ivMp4)
                ivDownload.isVisible = !checkWhetherDownloaded(pojo)
                tvTitle.text = pojo.title
                tvDescription.text = pojo.description
                ivDownload.setOnClickListener(this@MP4VH)
                btnPause.setOnClickListener(this@MP4VH)
                btnCancel.setOnClickListener(this@MP4VH)
                onFileStatus(pojo)
            }

        }

        @SuppressLint("TimberArgCount")
        private fun checkWhetherDownloaded(pojo: Pojo): Boolean {
            var boolean = false
            if (fileNames.isNotEmpty()) {
                for (i in fileNames) {
                    if (i == pojo.fileName) {
                        boolean = true
                        Timber.tag("checkWhetherDownloaded")
                            .d("%s%s", "%s and ", "i: " + i, pojo.fileName)
                    }
                }

            }
            return boolean
        }

        fun onFileDownloading(pojo: Pojo) {
            with(binding) {
                if (pojo.totalBytes != 0) {
                    val progressPercent =
                        pojo.soFarBytes.toFloat() * 100 / pojo.totalBytes.toFloat()
                    if (progressPercent > 0) {
                        pbDownloading.isIndeterminate = false
                        pbDownloading.progress = progressPercent.toInt()
                    }
                    var soFarBytes = pojo.soFarBytes.toFloat()
                    var totalBytes = pojo.totalBytes.toFloat()
                    var sofarText = "B"
                    var totalText = "B"
                    if (soFarBytes > 1_024 * 1_024) {
                        soFarBytes /= (1_024 * 1_024)
                        sofarText = "MB"
                    } else if (soFarBytes > 1_024) {
                        soFarBytes /= 1_024
                        sofarText = "KB"
                    }

                    if (totalBytes > 1_024 * 1_024) {
                        totalBytes /= (1_024 * 1_024)
                        totalText = "MB"
                    } else if (totalBytes > 1_024) {
                        totalBytes /= 1_024
                        totalText = "KB"
                    }

                    soFarBytes = round(soFarBytes, 1)
                    totalBytes = round(totalBytes, 1)
                    tvProgress.text =
                        "$statusText: ${progressPercent.toInt()}%  ${soFarBytes} $sofarText/$totalBytes $totalText"
                }
            }

        }

        fun onFileStatus(pojo: Pojo) {
            with(binding) {
                Log.d("status", "onFileStatus: ${pojo.title} ${pojo.status}")
                when (pojo.status) {
                    DownloadStatus.EMPTY -> {
                        llDownload.isVisible = false
                        ivDownload.isVisible = true
                        ivDownload.isClickable = true
                        statusText = ""
                    }
                    DownloadStatus.CONNECTED -> {
                        mDatalist[bindingAdapterPosition].paused = false
                        llDownload.isVisible = true
                        ivDownload.isVisible = false
                        ivDownload.isClickable = false
                        btnPause.isInvisible = false
                        statusText = "Downloading"
                    }
                    DownloadStatus.SUCCESS -> {
                        llDownload.isVisible = false
                        ivDownload.isVisible = false
                        ivDownload.isClickable = false
                        btnPause.isInvisible = true
                        llcItemRow.setOnClickListener(this@MP4VH)
                        statusText = "Download completed"
                    }
                    DownloadStatus.ERROR -> {
                        llDownload.isVisible = true
                        ivDownload.isVisible = true
                        ivDownload.isClickable = true
                        statusText = "Error downloading"
                    }
                    DownloadStatus.PAUSED -> {
                        Log.d("TAGTAG", "onFileStatus: PAUSED")
                        mDatalist[bindingAdapterPosition].paused = true
                        llDownload.isVisible = mDatalist[bindingAdapterPosition].soFarBytes > 0
                        ivDownload.isVisible = false
                        ivDownload.isClickable = false
                        statusText = "Downloading paused"
                    }
                    DownloadStatus.CANCEL -> {
                        Log.d("TAGTAG", "onFileStatus: CANCEL")
                        pojo.soFarBytes = 0
                        binding.pbDownloading.isIndeterminate = true
                        binding.ivDownload.isVisible = true
                        binding.ivDownload.isClickable = true
                        binding.llDownload.isVisible = false
                    }
                }


            }

            onFileDownloading(pojo)

        }

        private fun round(d: Float, decimalPlace: Int): Float {
            var bd = BigDecimal(d.toString())
            bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP)
            return bd.toFloat()
        }

        override fun onClick(v: View) {
            with(binding) {
                when (v.id) {
                    ivDownload.id -> {
                        ivDownload.visibility = View.GONE
                        val pojo = mDatalist[bindingAdapterPosition]
                        val fileName = mDatalist[bindingAdapterPosition].title.replace(" ", "")
                        llDownload.isVisible = true
                        btnPause.setImageResource(R.drawable.ic_pause_circle)
                        val id =
                            onClickListener.onClickDownload(
                                bindingAdapterPosition,
                                fileName,
                                pojo.sources[0]
                            )
                        pojo.fileName = fileName
                        pojo.id = id
                    }
                    btnPause.id -> {
                        val pojo = mDatalist[bindingAdapterPosition]
                        if (pojo.paused) {
                            //pause -> downloading
                            pojo.paused = false
                            btnPause.setImageResource(R.drawable.ic_pause_circle)
                            onClickListener.onClickPlay(
                                bindingAdapterPosition,
                                pojo.fileName,
                                pojo.sources[0]
                            )
                        } else {
                            //downloading -> pause
                            pojo.paused = true
                            btnPause.setImageResource(R.drawable.ic_play_circle)
                            onClickListener.onClickPause(pojo.id)
                        }
                    }
                    btnCancel.id -> {
                        val pojo = mDatalist[bindingAdapterPosition]
                        pojo.soFarBytes = 0
                        onClickListener.onClickCancel(
                            bindingAdapterPosition,
                            pojo.id,
                            pojo.fileName
                        )
                    }
                    llcItemRow.id -> {
                        val pojo = mDatalist[bindingAdapterPosition]
                        onClickListener.onClickInfo(
                            bindingAdapterPosition,
                            pojo.fileName,
                            pojo.title,
                            pojo.description
                        )

                    }

                }
            }
        }
    }

    fun setStatus(position: Int, status: DownloadStatus, payloads: MP4Payloads) {
        mDatalist[position].status = status
        notifyItemChanged(position, payloads)
    }

    fun setStatus(position: Int, soFarBytes: Int, totalBytes: Int, payloads: MP4Payloads) {
        mDatalist[position].soFarBytes = soFarBytes
        mDatalist[position].totalBytes = totalBytes
        notifyItemChanged(position, payloads)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MP4VH {
        val binding = ItemRowMp4Binding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MP4VH(binding, onClickListener, fileNames, mDatalist)
    }

    override fun onBindViewHolder(holder: MP4VH, position: Int) {
        holder.onBind(mDatalist[position])
    }

    override fun onBindViewHolder(
        holder: MP4VH,
        position: Int,
        payloads: MutableList<Any>
    ) {
        if (payloads.isNotEmpty()) {
            when (payloads[0]) {
                MP4Payloads.FILEDOWNLOADING -> {
                    holder.onFileDownloading(mDatalist[position])
                }
                MP4Payloads.FILESTATUS -> {
                    holder.onFileStatus(mDatalist[position])
                }
            }
        } else {
            super.onBindViewHolder(holder, position, payloads)
        }
    }

    override fun getItemCount() = mDatalist.size


}

interface IOnItemClickListener {
    fun onClickInfo(position: Int, fileName: String, title: String, description: String)
    fun onClickDownload(position: Int, fileName: String, url: String): Int
    fun onClickPlay(position: Int, fileName: String, url: String)
    fun onClickPause(id: Int)
    fun onClickCancel(pos: Int, id: Int, fileName: String)
}


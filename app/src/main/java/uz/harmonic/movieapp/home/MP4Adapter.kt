package uz.harmonic.movieapp.home

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import uz.harmonic.movieapp.R
import uz.harmonic.movieapp.common.Constants
import uz.harmonic.movieapp.data.DownloadStatus
import uz.harmonic.movieapp.data.MP4Payloads
import uz.harmonic.movieapp.data.Pojo

import uz.harmonic.movieapp.databinding.ItemRowMp4Binding
import java.math.BigDecimal

class MP4Adapter(
    private val mDatalist: MutableList<Pojo>,
    private val onClickListener: IOnItemClickListener,
    private val fileNames: ArrayList<String>?
) : RecyclerView.Adapter<MP4Adapter.MP4VH>() {

    inner class MP4VH(
        private val binding: ItemRowMp4Binding,
        private val onClickListener: IOnItemClickListener
    ) : RecyclerView.ViewHolder(binding.root), View.OnClickListener {
        private var statusText: String = "status"

        fun onBind(pojo: Pojo) {
            binding.ivDownload.isVisible = !checkWhetherDownloaded(pojo)
            binding.tvTitle.text = pojo.title
            binding.tvDescription.text = pojo.description
            Glide.with(binding.root.context)
                .load("${Constants.BASE_URL}${pojo.thumb}")
                .placeholder(R.drawable.ic_video_placeholder)
                .into(binding.ivMp4)
            binding.ivDownload.setOnClickListener(this)
            binding.btnPause.setOnClickListener(this)
            binding.btnCancel.setOnClickListener(this)
            onFileStatus(pojo)
        }

        private fun checkWhetherDownloaded(pojo: Pojo): Boolean {
            var boolean = false
            if (fileNames != null) {
                for (i in fileNames) {
                    if (i == pojo.fileName) {
                        boolean = true
                    }
                }

            }
            return boolean
        }

        fun onFileDownloading(pojo: Pojo) {
            if (pojo.totalBytes != 0) {
                val progressPercent = pojo.soFarBytes.toFloat() * 100 / pojo.totalBytes.toFloat()
                if (progressPercent > 0) {
                    binding.pbDownloading.isIndeterminate = false
                    binding.pbDownloading.progress = progressPercent.toInt()
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
                binding.tvProgress.text =
                    "$statusText: ${progressPercent.toInt()}%  ${soFarBytes} $sofarText/$totalBytes $totalText"
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
            when (v.id) {
                binding.ivDownload.id -> {
                    binding.ivDownload.visibility = View.GONE
                    val pojo = mDatalist[bindingAdapterPosition]
                    val fileName = mDatalist[bindingAdapterPosition].title.replace(" ", "")
                    binding.llDownload.isVisible = true
                    binding.btnPause.setImageResource(R.drawable.ic_pause_circle)
                    val id =
                        onClickListener.onClickDownload(
                            bindingAdapterPosition,
                            fileName,
                            pojo.sources[0]
                        )
                    pojo.fileName = fileName
                    pojo.id = id
                }
                binding.btnPause.id -> {
                    val pojo = mDatalist[bindingAdapterPosition]
                    if (pojo.paused) {
                        //pause -> downloading
                        pojo.paused = false
                        binding.btnPause.setImageResource(R.drawable.ic_pause_circle)
                        onClickListener.onClickPlay(
                            bindingAdapterPosition,
                            pojo.fileName,
                            pojo.sources[0]
                        )
                    } else {
                        //downloading -> pause
                        pojo.paused = true
                        binding.btnPause.setImageResource(R.drawable.ic_play_circle)
                        onClickListener.onClickPause(pojo.id)
                    }
                }
                binding.btnCancel.id -> {
//                    mDatalist[bindingAdapterPosition].paused = false
                    val pojo = mDatalist[bindingAdapterPosition]
                    pojo.soFarBytes = 0
                    onClickListener.onClickCancel(bindingAdapterPosition, pojo.id, pojo.fileName)
                    /*  binding.pbDownloading.isIndeterminate = true
                      binding.ivDownload.isVisible = true
                      binding.ivDownload.isClickable = true
                      binding.llDownload.isVisible = false*/
                }
                binding.llcItemRow.id -> {
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
        return MP4VH(binding, onClickListener)
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


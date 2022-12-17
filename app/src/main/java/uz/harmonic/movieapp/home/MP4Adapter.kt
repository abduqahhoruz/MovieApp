package uz.harmonic.movieapp.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import uz.harmonic.movieapp.R
import uz.harmonic.movieapp.data.DownloadStatus
import uz.harmonic.movieapp.data.MP4Payloads
import uz.harmonic.movieapp.data.Pojo
import uz.harmonic.movieapp.databinding.ItemRowMp4Binding
import java.math.BigDecimal

class MP4Adapter(
    private val mDatalist: MutableList<Pojo>,
    private val onClickListener: IOnItemClickListener,
    private val fileNames: List<String>
) : ListAdapter<Pojo, MP4Adapter.MP4VH>(ItemMp4Difference()) {

    class MP4VH(
        private val binding: ItemRowMp4Binding,
        private val onClickListener: IOnItemClickListener
    ) : RecyclerView.ViewHolder(binding.root), View.OnClickListener {
        private var statusText: String = "status"
        lateinit var pojo: Pojo

        fun onBind(pojo: Pojo) {
            this.pojo = pojo
            with(binding) {
                tvTitle.text = pojo.title
                tvDescription.text = pojo.url
                ivDownload.setOnClickListener(this@MP4VH)
                btnPause.setOnClickListener(this@MP4VH)
                btnCancel.setOnClickListener(this@MP4VH)
                ivDelete.setOnClickListener(this@MP4VH)
                onFileStatus(pojo)
            }

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
                when (pojo.status) {
                    DownloadStatus.EMPTY -> {
                        llDownload.isVisible = false
                        ivDownload.isVisible = true
                        ivDownload.isClickable = true
                        statusText = ""
                    }
                    DownloadStatus.CONNECTED -> {
                        btnPause.setImageResource(R.drawable.ic_pause_circle)
                        llDownload.isVisible = true
                        ivDownload.isVisible = false
                        ivDownload.isClickable = false
                        statusText = "Downloading"
                    }
                    DownloadStatus.SUCCESS -> {
                        llDownload.isVisible = false
                        ivDownload.setImageResource(R.drawable.ic_play_circle)
                        ivDownload.isVisible = true
                        ivDownload.isClickable = true
                        statusText = "Download completed"
                    }
                    DownloadStatus.ERROR -> {
                        llDownload.isVisible = true
                        ivDownload.isVisible = true
                        ivDownload.isClickable = true
                        statusText = "Error downloading"
                    }

                    DownloadStatus.CANCEL -> {
                        pojo.soFarBytes = 0
                        binding.pbDownloading.isIndeterminate = true
                        binding.ivDownload.isVisible = true
                        binding.ivDownload.isClickable = true
                        binding.llDownload.isVisible = false
                    }
                    else -> {}
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
                        onClickListener.onClickDownload(bindingAdapterPosition)
                    }
                    btnCancel.id -> {
                        onClickListener.onClickPause(bindingAdapterPosition)
                        onClickListener.onClickCancel(bindingAdapterPosition)
                    }
                    ivDelete.id -> {
                        onClickListener.onClickPause(bindingAdapterPosition)
                        onClickListener.onClickDelete(bindingAdapterPosition)
                    }

                    else -> {

                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MP4VH {
        val binding = ItemRowMp4Binding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MP4VH(binding, onClickListener)
    }

    override fun onBindViewHolder(holder: MP4VH, position: Int) {
        holder.onBind(mDatalist[position])
    }

    override fun onBindViewHolder(
        holder: MP4VH, position: Int, payloads: MutableList<Any>
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


}

class ItemMp4Difference : DiffUtil.ItemCallback<Pojo>() {
    override fun areItemsTheSame(
        oldItem: Pojo, newItem: Pojo
    ): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(
        oldItem: Pojo, newItem: Pojo
    ): Boolean {
        return oldItem.id == newItem.id

    }

}


interface IOnItemClickListener {
    fun onClickDelete(position: Int)
    fun onClickDownload(position: Int)
    fun onClickPlay(position: Int)
    fun onClickPause(position: Int)
    fun onClickCancel(position: Int)
}


package si.uni_lj.fri.pecd2024_mp_2

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ProgressBar
import android.widget.TextView

class ResultAdapter(private val dataSet: ArrayList<Result>, var mContext: Context) :
    ArrayAdapter<Result?>(mContext, R.layout.row_item, dataSet as List<Result?>), View.OnClickListener {
    private class ViewHolder {
        var txtLabel: TextView? = null
        var txtValue: TextView? = null
        var txtLoudness: TextView? = null
        var progressBar: ProgressBar? = null
    }

    override fun onClick(view: View) {
    }

    @SuppressLint("DefaultLocale")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        val result = getItem(position)
        val viewHolder: ViewHolder

        if (convertView == null) {
            viewHolder = ViewHolder()
            convertView = View.inflate(context, R.layout.row_item, null)
            viewHolder.txtLabel = convertView.findViewById<View>(R.id.txtLabel) as TextView
            viewHolder.txtValue = convertView.findViewById<View>(R.id.txtValue) as TextView
            viewHolder.txtLoudness = convertView.findViewById<View>(R.id.txtLoudness) as TextView
            viewHolder.progressBar = convertView.findViewById<View>(R.id.progressBar) as ProgressBar
            convertView.setTag(viewHolder)
        } else {
            viewHolder = convertView.tag as ViewHolder
        }
        viewHolder.txtLabel!!.text = result!!.label
        viewHolder.txtLoudness!!.text = result!!.loudness.toString()
        val confidence1 = result.confidence?.times(100)
        viewHolder.txtValue!!.text = String.format("%.2f", confidence1)
        viewHolder.progressBar!!.progress = (result.confidence?.times(100))?.toInt() ?: 0

        return convertView!!
    }
}

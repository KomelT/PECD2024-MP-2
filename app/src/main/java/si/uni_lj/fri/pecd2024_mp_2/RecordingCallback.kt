package si.uni_lj.fri.pecd2024_mp_2

interface RecordingCallback {
    fun onDataUpdated(data: ArrayList<Result>)
}
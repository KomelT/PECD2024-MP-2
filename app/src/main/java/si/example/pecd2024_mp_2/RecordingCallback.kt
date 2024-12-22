package si.example.pecd2024_mp_2

interface RecordingCallback {
    fun onDataUpdated(data: ArrayList<Result>)
}
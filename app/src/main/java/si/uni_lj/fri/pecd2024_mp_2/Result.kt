package si.uni_lj.fri.pecd2024_mp_2

class Result {
    var label: String? = null
    var confidence: Double? = null
    var loudness: Double? = null

    constructor(label: String?, confidence: Double?, loudness: Double?) {
        this.label = label
        this.confidence = confidence
        this.loudness = loudness
    }

    constructor()
}

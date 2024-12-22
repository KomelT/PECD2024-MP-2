package si.example.pecd2024_mp_2

class Result {
    var label: String? = null
    var confidence: Double? = null

    constructor(label: String?, confidence: Double?) {
        this.label = label
        this.confidence = confidence
    }

    constructor()
}

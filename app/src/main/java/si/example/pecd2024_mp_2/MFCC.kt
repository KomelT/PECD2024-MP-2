package si.example.pecd2024_mp_2

import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Wav file abstraction layer.
 * Source based on http://www.labbookpages.co.uk/audio/javaWavFiles.html
 *
 *
 */
class MFCC {
    var fft: FFT = FFT()


    fun setSampleRate(sampleRateVal: Int) {
        sampleRate = sampleRateVal.toDouble()
    }

    fun setN_mfcc(n_mfccVal: Int) {
        n_mfcc = n_mfccVal
    }

    fun process(doubleInputBuffer: DoubleArray): FloatArray {
        val mfccResult = dctMfcc(doubleInputBuffer)
        return finalshape(mfccResult)
    }

    //MFCC into 1d
    private fun finalshape(mfccSpecTro: Array<DoubleArray>): FloatArray {
        val finalMfcc = FloatArray(mfccSpecTro[0].size * mfccSpecTro.size)
        var k = 0
        for (i in mfccSpecTro[0].indices) {
            for (j in mfccSpecTro.indices) {
                finalMfcc[k] = mfccSpecTro[j][i].toFloat()
                k = k + 1
            }
        }
        return finalMfcc
    }

    //DCT to mfcc, librosa
    private fun dctMfcc(y: DoubleArray): Array<DoubleArray> {
        val specTroGram = powerToDb(melSpectrogram(y))
        val dctBasis = dctFilter(n_mfcc, n_mels)
        val mfccSpecTro = Array(n_mfcc) { DoubleArray(specTroGram[0].size) }
        for (i in 0 until n_mfcc) {
            for (j in specTroGram[0].indices) {
                for (k in specTroGram.indices) {
                    mfccSpecTro[i][j] += dctBasis[i][k] * specTroGram[k][j]
                }
            }
        }
        return mfccSpecTro
    }


    //mel spectrogram, librosa
    private fun melSpectrogram(y: DoubleArray): Array<DoubleArray> {
        val melBasis = melFilter()
        val spectro = stftMagSpec(y)
        val melS = Array(melBasis.size) { DoubleArray(spectro[0].size) }
        for (i in melBasis.indices) {
            for (j in spectro[0].indices) {
                for (k in melBasis[0].indices) {
                    melS[i][j] += melBasis[i][k] * spectro[k][j]
                }
            }
        }
        return melS
    }


    //stft, librosa
    private fun stftMagSpec(y: DoubleArray): Array<DoubleArray> {
        //Short-time Fourier transform (STFT)
        val fftwin = window
        //pad y with reflect mode so it's centered. This reflect padding implementation is
        // not perfect but works for this demo.
        val ypad = DoubleArray(n_fft + y.size)
        for (i in 0 until n_fft / 2) {
            ypad[(n_fft / 2) - i - 1] = y[i + 1]
            ypad[(n_fft / 2) + y.size + i] = y[y.size - 2 - i]
        }
        for (j in y.indices) {
            ypad[(n_fft / 2) + j] = y[j]
        }


        val frame = yFrame(ypad)
        val fftmagSpec = Array(1 + n_fft / 2) { DoubleArray(frame[0].size) }
        val fftFrame = DoubleArray(n_fft)
        for (k in frame[0].indices) {
            for (l in 0 until n_fft) {
                fftFrame[l] = fftwin[l] * frame[l][k]
            }
            val magSpec = magSpectrogram(fftFrame)
            for (i in 0 until 1 + n_fft / 2) {
                fftmagSpec[i][k] = magSpec[i]
            }
        }
        return fftmagSpec
    }

    private fun magSpectrogram(frame: DoubleArray): DoubleArray {
        val magSpec = DoubleArray(frame.size)
        fft.process(frame)
        for (m in frame.indices) {
            magSpec[m] = fft.real[m] * fft.real[m] + fft.imag[m] * fft.imag[m]
        }
        return magSpec
    }


    private val window: DoubleArray
        //get hann window, librosa
        get() {
            //Return a Hann window for even n_fft.
            //The Hann window is a taper formed by using a raised cosine or sine-squared
            //with ends that touch zero.
            val win = DoubleArray(n_fft)
            for (i in 0 until n_fft) {
                win[i] =
                    0.5 - 0.5 * cos(2.0 * Math.PI * i / n_fft)
            }
            return win
        }

    //frame, librosa
    private fun yFrame(ypad: DoubleArray): Array<DoubleArray> {
        val n_frames = 1 + (ypad.size - n_fft) / hop_length
        val winFrames = Array(n_fft) { DoubleArray(n_frames) }
        for (i in 0 until n_fft) {
            for (j in 0 until n_frames) {
                winFrames[i][j] = ypad[j * hop_length + i]
            }
        }
        return winFrames
    }

    //power to db, librosa
    private fun powerToDb(melS: Array<DoubleArray>): Array<DoubleArray> {
        //Convert a power spectrogram (amplitude squared) to decibel (dB) units
        //  This computes the scaling ``10 * log10(S / ref)`` in a numerically
        //  stable way.
        val log_spec = Array(melS.size) { DoubleArray(melS[0].size) }
        var maxValue = -100.0
        for (i in melS.indices) {
            for (j in melS[0].indices) {
                val magnitude = abs(melS[i][j])
                if (magnitude > 1e-10) {
                    log_spec[i][j] = 10.0 * log10(magnitude)
                } else {
                    log_spec[i][j] = 10.0 * (-10)
                }
                if (log_spec[i][j] > maxValue) {
                    maxValue = log_spec[i][j]
                }
            }
        }

        //set top_db to 80.0
        for (i in melS.indices) {
            for (j in melS[0].indices) {
                if (log_spec[i][j] < maxValue - 80.0) {
                    log_spec[i][j] = maxValue - 80.0
                }
            }
        }
        //ref is disabled, maybe later.
        return log_spec
    }

    //dct, librosa
    private fun dctFilter(n_filters: Int, n_input: Int): Array<DoubleArray> {
        //Discrete cosine transform (DCT type-III) basis.
        val basis = Array(n_filters) { DoubleArray(n_input) }
        val samples = DoubleArray(n_input)
        for (i in 0 until n_input) {
            samples[i] = (1 + 2 * i) * Math.PI / (2.0 * (n_input))
        }
        for (j in 0 until n_input) {
            basis[0][j] = 1.0 / sqrt(n_input.toDouble())
        }
        for (i in 1 until n_filters) {
            for (j in 0 until n_input) {
                basis[i][j] = cos(i * samples[j]) * sqrt(2.0 / (n_input))
            }
        }
        return basis
    }


    //mel, librosa
    private fun melFilter(): Array<DoubleArray> {
        //Create a Filterbank matrix to combine FFT bins into Mel-frequency bins.
        // Center freqs of each FFT bin
        val fftFreqs = fftFreq()
        //'Center freqs' of mel bands - uniformly spaced between limits
        val melF = melFreq(n_mels + 2)

        val fdiff = DoubleArray(melF.size - 1)
        for (i in 0 until melF.size - 1) {
            fdiff[i] = melF[i + 1] - melF[i]
        }

        val ramps = Array(melF.size) { DoubleArray(fftFreqs.size) }
        for (i in melF.indices) {
            for (j in fftFreqs.indices) {
                ramps[i][j] = melF[i] - fftFreqs[j]
            }
        }

        val weights = Array(n_mels) { DoubleArray(1 + n_fft / 2) }
        for (i in 0 until n_mels) {
            for (j in fftFreqs.indices) {
                val lowerF = -ramps[i][j] / fdiff[i]
                val upperF = ramps[i + 2][j] / fdiff[i + 1]
                if (lowerF > upperF && upperF > 0) {
                    weights[i][j] = upperF
                } else if (lowerF > upperF && upperF < 0) {
                    weights[i][j] = 0.0
                } else if (lowerF < upperF && lowerF > 0) {
                    weights[i][j] = lowerF
                } else if (lowerF < upperF && lowerF < 0) {
                    weights[i][j] = 0.0
                } else {
                }
            }
        }

        val enorm = DoubleArray(n_mels)
        for (i in 0 until n_mels) {
            enorm[i] = 2.0 / (melF[i + 2] - melF[i])
            for (j in fftFreqs.indices) {
                weights[i][j] *= enorm[i]
            }
        }
        return weights

        //need to check if there's an empty channel somewhere
    }

    //fft frequencies, librosa
    private fun fftFreq(): DoubleArray {
        //Alternative implementation of np.fft.fftfreqs
        val freqs = DoubleArray(1 + n_fft / 2)
        for (i in 0 until 1 + n_fft / 2) {
            freqs[i] = 0 + (sampleRate / 2) / (n_fft / 2) * i
        }
        return freqs
    }

    //mel frequencies, librosa
    private fun melFreq(numMels: Int): DoubleArray {
        //'Center freqs' of mel bands - uniformly spaced between limits
        val LowFFreq = DoubleArray(1)
        val HighFFreq = DoubleArray(1)
        LowFFreq[0] = fMin
        HighFFreq[0] = fMax
        val melFLow = freqToMel(LowFFreq)
        val melFHigh = freqToMel(HighFFreq)
        val mels = DoubleArray(numMels)
        for (i in 0 until numMels) {
            mels[i] = melFLow[0] + (melFHigh[0] - melFLow[0]) / (numMels - 1) * i
        }
        return melToFreq(mels)
    }


    //mel to hz, htk, librosa
    private fun melToFreqS(mels: DoubleArray): DoubleArray {
        val freqs = DoubleArray(mels.size)
        for (i in mels.indices) {
            freqs[i] = 700.0 * (10.0.pow(mels[i] / 2595.0) - 1.0)
        }
        return freqs
    }


    // hz to mel, htk, librosa
    protected fun freqToMelS(freqs: DoubleArray): DoubleArray {
        val mels = DoubleArray(freqs.size)
        for (i in freqs.indices) {
            mels[i] = 2595.0 * log10(1.0 + freqs[i] / 700.0)
        }
        return mels
    }

    //mel to hz, Slaney, librosa
    private fun melToFreq(mels: DoubleArray): DoubleArray {
        // Fill in the linear scale
        val f_min = 0.0
        val f_sp = 200.0 / 3
        val freqs = DoubleArray(mels.size)

        // And now the nonlinear scale
        val min_log_hz = 1000.0 // beginning of log region (Hz)
        val min_log_mel = (min_log_hz - f_min) / f_sp // same (Mels)
        val logstep = ln(6.4) / 27.0

        for (i in mels.indices) {
            if (mels[i] < min_log_mel) {
                freqs[i] = f_min + f_sp * mels[i]
            } else {
                freqs[i] = min_log_hz * exp(logstep * (mels[i] - min_log_mel))
            }
        }
        return freqs
    }


    // hz to mel, Slaney, librosa
    protected fun freqToMel(freqs: DoubleArray): DoubleArray {
        val f_min = 0.0
        val f_sp = 200.0 / 3
        val mels = DoubleArray(freqs.size)

        // Fill in the log-scale part
        val min_log_hz = 1000.0 // beginning of log region (Hz)
        val min_log_mel = (min_log_hz - f_min) / f_sp // # same (Mels)
        val logstep = ln(6.4) / 27.0 // step size for log region

        for (i in freqs.indices) {
            if (freqs[i] < min_log_hz) {
                mels[i] = (freqs[i] - f_min) / f_sp
            } else {
                mels[i] = min_log_mel + ln(freqs[i] / min_log_hz) / logstep
            }
        }
        return mels
    }

    // log10
    private fun log10(value: Double): Double {
        return ln(value) / ln(10.0)
    }

    companion object {
        private var n_mfcc = 13
        private const val fMin = 0.0
        private const val n_fft = 2048
        private const val hop_length = 512
        private const val n_mels = 26

        private var sampleRate = 16000.0
        private val fMax = sampleRate / 2.0
    }
}

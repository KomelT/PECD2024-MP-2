package si.example.pecd2024_mp_2

import kotlin.math.cos
import kotlin.math.ln
import kotlin.math.sin

/**
 * Fast Fourier Transform.
 *
 * last updated on June 15, 2002<br></br>
 * **description:** FFT class for real signals. Upon entry, N contains the
 * numbers of points in the DFT, real[] and imaginary[] contain the real and
 * imaginary parts of the input. Upon return, real[] and imaginary[] contain the
 * DFT output. All signals run from 0 to N - 1<br></br>
 * **input:** speech signal<br></br>
 * **output:** real and imaginary part of DFT output
 *
 * @author Danny Su
 * @author Hanns Holger Rutz
 */
class FFT {
    lateinit var real: DoubleArray
    lateinit var imag: DoubleArray

    /**
     * Performs Fast Fourier Transformation in place.
     */
    fun process(signal: DoubleArray) {
        val numPoints = signal.size
        // initialize real & imag array
        real = signal
        imag = DoubleArray(numPoints)

        // perform FFT using the real & imag array
        val pi = Math.PI
        val numStages = (ln(numPoints.toDouble()) / ln(2.0)).toInt()
        val halfNumPoints = numPoints shr 1
        var j = halfNumPoints
        // FFT time domain decomposition carried out by "bit reversal sorting"
        // algorithm
        var k: Int
        for (i in 1 until numPoints - 2) {
            if (i < j) {
                // swap
                val tempReal = real[j]
                val tempImag = imag[j]
                real[j] = real[i]
                imag[j] = imag[i]
                real[i] = tempReal
                imag[i] = tempImag
            }
            k = halfNumPoints
            while (k <= j) {
                j -= k
                k = k shr 1
            }
            j += k
        }

        // loop for each stage
        for (stage in 1..numStages) {
            var LE = 1
            for (i in 0 until stage) {
                LE = LE shl 1
            }
            val LE2 = LE shr 1
            var UR = 1.0
            var UI = 0.0
            // calculate sine & cosine values
            val SR = cos(pi / LE2)
            val SI = -sin(pi / LE2)
            // loop for each sub DFT
            for (subDFT in 1..LE2) {
                // loop for each butterfly
                var butterfly = subDFT - 1
                while (butterfly <= numPoints - 1) {
                    val ip = butterfly + LE2
                    // butterfly calculation
                    val tempReal = (real[ip] * UR - imag[ip] * UI)
                    val tempImag = (real[ip] * UI + imag[ip] * UR)
                    real[ip] = real[butterfly] - tempReal
                    imag[ip] = imag[butterfly] - tempImag
                    real[butterfly] += tempReal
                    imag[butterfly] += tempImag
                    butterfly += LE
                }

                val tempUR = UR
                UR = tempUR * SR - UI * SI
                UI = tempUR * SI + UI * SR
            }
        }
    }
}
package movement

import org.apache.commons.math3.analysis.UnivariateFunction
import org.apache.commons.math3.analysis.integration.SimpsonIntegrator
import org.apache.commons.math3.analysis.interpolation.SplineInterpolator
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction
import java.util.*
import kotlin.math.pow
import kotlin.math.sqrt
import kotlinx.coroutines.*

class LegMovement(
    private val points: Array<Point3D>
): MovementManager {

    // Extract x, y, and z coordinates into separate arrays
    private val xCoordinates = points.map { it.x }.toDoubleArray()
    private val yCoordinates = points.map { it.y }.toDoubleArray()
    private val zCoordinates = points.map { it.z }.toDoubleArray()

    // total length of the spline
    private val splineLength: Double

    //.length already covered
    private var coveredLength = 0.0

    // Interpolate x, y, and z coordinates separately
    private val splineInterpolator = SplineInterpolator()
    // Interpolate x, y, and z coordinates separately
    private val xFunction: PolynomialSplineFunction = splineInterpolator.interpolate(xCoordinates, xCoordinates) // Interpolate against x coordinates
    private val yFunction: PolynomialSplineFunction = splineInterpolator.interpolate(xCoordinates, yCoordinates)
    private val zFunction: PolynomialSplineFunction = splineInterpolator.interpolate(xCoordinates, zCoordinates)


    init {
        // Calculate the total length of the spline
        val integrator = SimpsonIntegrator()
        val intervalStart = xFunction.knots[0]
        val intervalEnd = xFunction.knots.last()
        splineLength = integrator.integrate(1000, { t ->
            sqrt(
                xFunction.derivative().value(t).pow(2.0) +
                        yFunction.derivative().value(t).pow(2.0) +
                        zFunction.derivative().value(t).pow(2.0)
            )
        }, intervalStart, intervalEnd).let { if (it.isNaN()) 0.0 else it }

    }

    override suspend fun getCoordinatesAtNext(stepLength: Double): Point3D {
        require(stepLength in 0.0 .. splineLength) { "Length must be within range [0, $splineLength" }

        // actual length for the function
        val wantedLength = if (coveredLength + stepLength > splineLength) stepLength else coveredLength + stepLength

        // Perform binary search to find the point corresponding to the specified length
        var low = xFunction.knots[0]
        var high = xFunction.knots.last()
        var mid = 0.0

        repeat(100) { // Maximum 100 iterations for safety
            mid = (low + high) / 2
            val currentLength = calculateLengthAtPoint(mid)

            if (currentLength < wantedLength) {
                low = mid
            } else if (currentLength > wantedLength) {
                high = mid
            } else {
                return Point3D(xFunction.value(mid), yFunction.value(mid), zFunction.value(mid))
            }
        }

        // Linear interpolation to get coordinates at the specified length
        val x = xFunction.value(mid)
        val y = yFunction.value(mid)
        val z = zFunction.value(mid)
        return roundCoordinates(Point3D(x, y, z))
    }

    override suspend fun getCoordinatesAtLength(length: Double): Point3D {
        require(length in 0.0..splineLength) { "Length must be within the range [0, $splineLength]" }

        // Perform binary search to find the point corresponding to the specified length
        var low = xFunction.knots[0]
        var high = xFunction.knots.last()
        var mid = 0.0

        repeat(100) { // Maximum 100 iterations for safety
            mid = (low + high) / 2
            val currentLength = calculateLengthAtPoint(mid)

            if (currentLength < length) {
                low = mid
            } else if (currentLength > length) {
                high = mid
            } else {
                return Point3D(xFunction.value(mid), yFunction.value(mid), zFunction.value(mid))
            }
        }

        // Linear interpolation to get coordinates at the specified length
        val x = xFunction.value(mid)
        val y = yFunction.value(mid)
        val z = zFunction.value(mid)
        return roundCoordinates(Point3D(x, y, z))
    }

    private fun calculateLengthAtPoint(point: Double): Double {
        val integrator = SimpsonIntegrator()
        val intervalStart = xFunction.knots[0]
        val intervalEnd = point
        return integrator.integrate(1000, UnivariateFunction { t ->
            Math.sqrt(
                xFunction.derivative().value(t).pow(2.0) +
                        yFunction.derivative().value(t).pow(2.0) +
                        zFunction.derivative().value(t).pow(2.0)
            )
        }, intervalStart, intervalEnd)
    }


    fun printSplineLength(): Double{
        println("Length of the spline is $splineLength")
        return splineLength
    }


    override fun nextCoordinates(): Point3D {
        val evaluationPoint = -80.0
        val interpolatedX = xFunction.value(evaluationPoint)
        val interpolatedY = yFunction.value(evaluationPoint)
        val interpolatedZ = zFunction.value(evaluationPoint)

        println("Interpolated value at $evaluationPoint: ($interpolatedX, $interpolatedY, $interpolatedZ)")
        return Point3D(0.0, 0.0, 0.0)
    }

    // round coordinates to 3 decimal spaces
    private fun roundCoordinates(coordinates: Point3D): Point3D {
        val format = "%.3f"
        val roundedX = String.format(Locale.ENGLISH, format, coordinates.x).toDouble()
        val roundedY = String.format(Locale.ENGLISH, format, coordinates.y).toDouble()
        val roundedZ = String.format(Locale.ENGLISH, format, coordinates.z).toDouble()
        return Point3D(roundedX, roundedY, roundedZ)
    }
}
enum class LegSide(){
    LEFT, RIGHT
}

data class Point3D(val x: Double, val y: Double, val z: Double)
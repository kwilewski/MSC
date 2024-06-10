import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import movement.LegMovement
import movement.Point3D
import kotlin.math.*

class Leg(
    // coordinates of the base of the leg
          val basePoint3D: Point3D,

    // length of elements of the leg
          val coxa: Double,
          val femur: Double,
          val tibia: Double
) {

    // current angles of servos
    var currentAlpha: Double = 0.0
    var currentBeta: Double = 0.0
    var currentGamma: Double = 0.0

    // target angles of servos
    var targetAlpha: Double = 0.0
    var targetBeta: Double = 0.0
    var targetGamma: Double = 0.0

    // current position of coxa / femur joint
    var currentFemurX: Double = 0.0
    var currentFemurY: Double = 0.0
    var currentFemurZ: Double = 0.0

    // target position of coxa / femur joint
    var targetFemurX: Double = 0.0
    var targetFemurY: Double = 0.0
    var targetFemurZ: Double = 0.0

    // current position of femur / tibia joint
    var currentTibiaX: Double = 0.0
    var currentTibiaY: Double = 0.0
    var currentTibiaZ: Double = 0.0

    // target position of femur / tibia joint
    var targetTibiaX: Double = 0.0
    var targetTibiaY: Double = 0.0
    var targetTibiaZ: Double = 0.0

    // current position of the leg
    var currentX: Double = 0.0
    var currentY: Double = 0.0
    var currentZ: Double = 0.0

    // target position of the leg
    private var targetPoint3D: Point3D? = null
    var targetX: Double = 0.0
    var targetY: Double = 0.0
    var targetZ: Double = 0.0

    // array of path defining points
    private var thruPointArray: Array<Point3D>? = null

    // time between next points in millis
    val stepTimeInMillis = 100L

    // distance of one step
    val stepDistance = 10.0

    private lateinit var movement: LegMovement


    fun initializeInterpolation(thruPoints: Array<Point3D>){
        this.thruPointArray = thruPoints
        movement = LegMovement(thruPoints)
    }

    fun startMovement(){
        GlobalScope.launch(Dispatchers.Main) {
            targetPoint3D = movement.getCoordinatesAtNext(stepDistance)
            calculateAngles()


        }
    }

    fun setCoordinates(x: Double, y: Double, z: Double){
        updateCurrentValues()
        this.targetX = x
        this.targetY = y
        this.targetZ = z
        calculateAngles()
    }

    private fun updateCurrentValues(){
        currentX = targetX
        currentY = targetY
        currentZ = targetZ
        currentAlpha = targetAlpha
        currentBeta = targetBeta
        currentGamma = targetGamma
        currentFemurX = targetFemurX
        currentFemurY = targetFemurY
        currentFemurZ = targetFemurZ
        currentTibiaX = targetTibiaX
        currentTibiaY = targetTibiaY
        currentTibiaZ = targetTibiaZ
    }


    private fun calculateAngles(){
        val totalX = targetPoint3D!!.x - basePoint3D.x
        val totalY = targetPoint3D!!.y - basePoint3D.y
        val totalZ = targetPoint3D!!.z - basePoint3D.z

        targetAlpha = atan((targetPoint3D!!.y - basePoint3D.y)/(totalX))

        // intermediate coords of coxa / femur
        targetFemurX = coxa * cos(targetAlpha)
        targetFemurY = coxa * sin(targetAlpha)

        val xBetaToTip = totalX - targetFemurX
        val yBetaToTip = totalY - targetFemurY
        val horizontalBetaToTip = xBetaToTip * cos(targetAlpha)
        val totalBetaToTip = sqrt(horizontalBetaToTip.pow(2) + totalZ.pow(2))

        val phi3 = asin(abs(totalZ) / totalBetaToTip)
        val phi2 = acos((tibia.pow(2) + totalBetaToTip.pow(2) - femur.pow(2)) / (2 * tibia * totalBetaToTip))
        val phi1 = acos((femur.pow(2) + totalBetaToTip.pow(2) - tibia.pow(2)) / (2 * femur * totalBetaToTip))


        targetBeta = if (totalZ > 0){
            phi1 + phi3
        } else {
            phi1 - phi3
        }

        targetGamma = phi1 + phi2



    }










}
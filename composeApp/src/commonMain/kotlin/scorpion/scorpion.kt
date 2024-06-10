import movement.LegMovement
import movement.Point3D

class Scorpion {

    val legLeft1 = Leg(Point3D(0.0, 100.0, 0.0), 80.0, 85.0, 150.0)
    val legLeft2 = Leg(Point3D(-100.0, 100.0, 0.0), 80.0, 100.0, 150.0)
    val legLeft3 = Leg(Point3D(-200.0, 100.0, 0.0), 80.0, 120.0, 150.0)
    val legLeft4 = Leg(Point3D(-300.0, 100.0, 0.0), 80.0, 140.0, 150.0)
    val legRight1 = Leg(Point3D(0.0, -100.0, 0.0), 80.0, 85.0, 150.0)
    val legRight2 = Leg(Point3D(-100.0, -100.0, 0.0), 80.0, 100.0, 150.0)
    val legRight3 = Leg(Point3D(-200.0, -100.0, 0.0), 80.0, 120.0, 150.0)
    val legRight4 = Leg(Point3D(-300.0, -100.0, 0.0), 80.0, 140.0, 150.0)

    fun controlLegLeft1(){

    }









    suspend fun setLeg1(){
        val movement = LegMovement(arrayOf(
            Point3D(-100.0, 100.0, 0.0),
            Point3D(100.0, 200.0, 20.0),
            Point3D(200.0, 100.0, 0.0)))
        val point = movement.nextCoordinates()
        val length = movement.printSplineLength()

        var i = 0.1
        while (i < length){
            val currentPoint = movement.getCoordinatesAtLength(i)
            println("Coordinates at length $i are $currentPoint")
            i += 5
        }
    }






}
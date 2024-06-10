package movement

interface MovementManager {


    fun nextCoordinates(): Point3D

    suspend fun getCoordinatesAtLength(length: Double): Point3D

    suspend fun getCoordinatesAtNext(stepLength: Double): Point3D
}
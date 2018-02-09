package com.builtbroken.mc.imp.transform.region

import java.math.{BigDecimal, MathContext, RoundingMode}

import com.builtbroken.jlib.data.vector.IPos2D
import com.builtbroken.mc.imp.transform.vector.Point

//TODO convert to java
class Rectangle(var min: Point, var max: Point) extends Shape2D(min.midpoint(max))
{
  def this() = this(new Point, new Point)

  def this(vec: Point, expansion: Double) = this(vec, vec.add(expansion))

  def this(minX: Double, minY: Double, maxX: Double, maxY: Double) = this(new Point(minX, minY), new Point(maxX, maxY))

  def this(rect: Rectangle) = this(rect.min.clone, rect.max.clone)

  /** Checks if the point is inside the shape */
  override def isWithin(p: IPos2D): Boolean = p.y >= this.min.y && p.y <= this.max.y && p.x >= this.min.x && p.x <= this.max.x

  def isWithin_rotated(p: IPos2D): Boolean =
  {
    //Rect corners
    val cornerB = this.cornerB()
    val cornerD = this.cornerD()

    //Area of the triangles made from the corners and p
    val areaAB = new Triangle(cornerA, cornerB, p).getArea
    val areaBC = new Triangle(cornerB, cornerC, p).getArea
    val areaCD = new Triangle(cornerC, cornerD, p).getArea
    val areaDA = new Triangle(cornerD, cornerA, p).getArea

    //If the area of the combined points is less and equals to area
    return (areaAB + areaBC + areaCD + areaDA) <= getArea
  }

  def cornerA() = min
  def cornerB() = new Point(min.x, max.y)
  def cornerC() = max
  def cornerD() = new Point(max.x, min.y)

  /**
   * Returns whether the given region intersects with this one.
   */
  def intersects(region: Rectangle): Boolean =
  {
    return if (region.max.x > this.min.x && region.min.x < this.max.x) (if (region.max.y > this.min.y && region.min.y < this.max.y) true else false) else false
  }

  override def getArea: Double = getSizeX * getSizeY

  override def getSizeX: Double = max.x - min.x

  override def getSizeY: Double = max.y - min.y


  override def toString: String =
  {
    val cont: MathContext = new MathContext(4, RoundingMode.HALF_UP)
    return "Rectangle[" + new BigDecimal(min.x, cont) + ", " + new BigDecimal(min.y, cont) + "] -> [" + new BigDecimal(max.x, cont) + ", " + new BigDecimal(max.y, cont) + "]"
  }

  override def equals(o: Any): Boolean =
  {
    if (o.isInstanceOf[Rectangle]) return (min == (o.asInstanceOf[Rectangle]).min) && (max == (o.asInstanceOf[Rectangle]).max)
    return false
  }

  override def clone: Rectangle = new Rectangle(this)
}
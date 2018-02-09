package com.builtbroken.mc.imp.transform.matrix

import com.builtbroken.jlib.data.vector.IPos3D
import com.builtbroken.mc.imp.transform.vector.Pos

import scala.Array._

/**
 * @author Calclavia
 */
object Matrix
{
  implicit def arrayToMatrix(m: Array[Array[Double]]) : Matrix = new Matrix(m)
}

class Matrix(val row: Int, val column: Int)
{
  val matrix = ofDim[Double](row, column)

  def this(m: Array[Array[Double]])
  {
    this(m.size, m(0).size)

    for (x <- 0 until m.size; y <- 0 until m(0).size)
    {
      matrix(x)(y) = m(x)(y)
    }
  }

  class MatrixAux(i: Int)
  {
    def apply(j: Int) = matrix(i)(j)

    def update(j: Int, value: Double) = matrix(i)(j) = value
  }

  def apply(i: Int) = new MatrixAux(i)

  /**
   * Multiplies two matrices (or with another column vector). This is non-commutative.
   */
  def *(otherMatrix: Matrix): Matrix =
  {
    val res = new Matrix(matrix.length, otherMatrix.column)

    for (row <- 0 until row; col <- 0 until otherMatrix.column; i <- 0 until column)
    {
      res(row)(col) += matrix(row)(i) * otherMatrix(i)(col)
    }

    return res
  }


  /**
   * Multiplies this column vector with a given matrix
 *
   * @param vector - A vector to be multiplied
   * @return The vector multiplied with the matrix.
   */
  def *(vector: IPos3D): Pos =
  {
    val newX = vector.x * matrix(0)(0) + vector.y * matrix(0)(1) + vector.z * matrix(0)(2)
    val newY = vector.x * matrix(1)(0) + vector.y * matrix(1)(1) + vector.z * matrix(1)(2)
    val newZ = vector.x * matrix(2)(0) + vector.y * matrix(2)(1) + vector.z * matrix(2)(2)
    return new Pos(newX, newY, newZ)
  }

  def multiply(otherMatrix: Matrix) = this * otherMatrix
}
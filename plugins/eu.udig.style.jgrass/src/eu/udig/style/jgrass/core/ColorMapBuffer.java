/*
 * JGrass - Free Open Source Java GIS http://www.jgrass.org 
 * (C) HydroloGIS - www.hydrologis.com 
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
 package eu.udig.style.jgrass.core;


import java.nio.ByteBuffer;




/**
 * 
 */
public class ColorMapBuffer
{
  private int dataRow = 0;  

  private ByteBuffer rgbBuffer = null;

  /** Creates a new instance of ColorMapBuffer */
  public ColorMapBuffer()
  {
  }

  public void setRowOffset(int row)
  {
    dataRow = row;
  }

  public int getRowOffset()
  {
    return dataRow;
  }

  public void setRGBBuffer(ByteBuffer rgb)
  {
    rgbBuffer = rgb;
  }

  public ByteBuffer getRGBBuffer()
  {
    return rgbBuffer;
  }
}

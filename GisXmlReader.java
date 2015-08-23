package gisxml;

/*
 * Directly read from ESRI GIS XML using JDOM and XPath()
 * Eliminated the GeoPlaces and everything is GeoShape
 * We did not add any Layer like GeoPlaces either
 * And this File process each of the GIS XML file similarly
 * even though they are written for different Objects
 * Some kind of generalization is introduced.   
 * 
 * 
 * This File is from Mostly_Graduates  written by Raihan Masud - Email : raihan@cs.uoregon.edu
 * 
 */


import java.io.*;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.xpath.*;
import org.jdom.input.*;
import org.jdom.JDOMException;

public class GisXmlReader  {
                 
	
	private String fileName;

	private GeoShape[] geoShapes;
	// max and min x and y coords
	//[0] -> min, [1] -> max
	private double[] max_min_x = new double [2];
	private boolean first_x;
	private double[] max_min_y = new double [2];
	private boolean first_y;

	
	/**
	 * 
	 * @param xmlFile
	 * @throws IOException 
	 * @throws JDOMException 
	 */
	public GisXmlReader(String xmlFile)  {
		fileName = xmlFile;
		
		//System.out.print(xmlFile);
		
		for(int i = 0; i <= 1; ++i){
			max_min_x[i] = 0;
			max_min_y[i] = 0;
		}
		
		first_x = true;
		first_y = true;
		
		readGISFile();
	}

	public GeoShape[] getShapes() {
		return geoShapes;
	}
	
	public double[] getMaxMinX(){
		return max_min_x;
	}
	public double[] getMaxMinY(){
		return max_min_y;
	}
	
		
	/**
	 * 
	 * @param group
	 * @param color
	 */
	
	
	public void readShapes(Element records, String color) {
	     //make vector of GeoShapes to store shapes that we read 
		Vector<GeoShape> shapes = new Vector<GeoShape>();
					   
		   String shapeID = "";
				   
		   //get two level up for the data set name
		   Element Dataset= records.getParentElement().getParentElement();
		   
		   //list all the <record> which store data  
		   List record = records.getChildren("Record");
		   		     
		   Iterator iterator =  record.iterator();
		   
		  
		   //visit all the records for geoshapes
		   while(iterator.hasNext())
		   
		   {
		   
		   
		    String shapeClass = "";
			String shapeName = "";
			List Points = null;	
		   //shapeClass tells the particular layer name(one class for each object)	
		   shapeClass= Dataset.getChildText("DatasetName");
		   
		   //assigning initial values shapeClass and shape name
		   //if some shape has particular name it would get changed later mostly with buildings
		   if(shapeClass.equals("Bicycle_Parking"))
           {
           shapeClass="bicycle_parking";
           shapeName ="Bicycle Parking";
           }

		   if(shapeClass.equals("Walk_Underpasses"))
		   {
		   shapeClass="walk_underpass";
		   shapeName ="walk_underpass";
		   }

		   if(shapeClass.equals("Buildings"))
		   {
		   shapeClass="building";
		   shapeName ="building";
		   }
		
		   if(shapeClass.equals("Public_Entrances"))
		   {
		   shapeClass="entrance";
		   shapeName ="entrance";
		   }
		
		   if(shapeClass.equals("Bus_Stops"))
		   {
		   shapeClass="bus_stop";
		   shapeName ="bus_stop";
		   }
		
		   if(shapeClass.equals("Athletic_Surfaces"))
		   {
		   shapeClass="athletic_surface_rubber";
		   shapeName ="athletic_surface_rubber";
		   }
		
		   if(shapeClass.equals("Streets"))
		   {
		   shapeClass="street";
		   shapeName ="street";
		   }
		
		   if(shapeClass.equals("Emergency_Callboxes"))
		   {
		   shapeClass="emergency_callbox";
		   shapeName ="Emergency Callbox";
		   }
		
		
		   if(shapeClass.equals("Hydrology"))
		   {
		   shapeClass="hydrology";
		   shapeName ="Body of Water";
		   }
		
		   if(shapeClass.equals("Parking"))
		   {
		   shapeClass="car_parking";
		   shapeName ="car_parking";
		   }
		
		   if(shapeClass.equals("Walks"))
		   {
		   shapeClass="walk";
		   shapeName ="walk";
		   }
		
		   if(shapeClass.equals("Stairways"))
		   {
		   shapeClass="stairway";
		   shapeName ="stairway";
		   }


		   //Now its time to read all the close paths for shapes
		   //current is the current <record> for shapes
		   Element current = (Element) iterator.next();
		   
		   
		   Element cur_shape_values = current.getChild("Values");
		   
		   //get the shape id from the 1st <Value> Element
		   shapeID = cur_shape_values.getChildText("Value");
		   
		   
		   GeoShape gShape =null;
		   
		   // we need to move to 2nd <Value> Element as the points/segments are inside for closed paths
		   List value_list = cur_shape_values.getChildren("Value");
		   Iterator value_iterator =  value_list.iterator();
		   int t=0;
		   
		   
		   
		   //find the 2nd <Value>  
		   while(value_iterator.hasNext())
		   {
			   t++;
		   
			   Element current_shape_value = (Element) value_iterator.next();
			   		   
		   if (t==2)//Find the 2nd <Value>
		   {
			   //Find the 3rd <Value> which has shape names
			   Element shapeNameElement = (Element) value_iterator.next();
			   
			 //assigning names to shapes if has name
			   //if don't then the class name is assigned to it previously works fine   
			   if (shapeNameElement!=null)  
			   { 
			   shapeName=shapeNameElement.getText();
			   }
			   
			   //make a new GeoShape object to assign property to it
			   gShape = new GeoShape(shapeName, shapeID);
		       gShape.setClassType(shapeClass);
			   //System.out.print("shape"+shapeName+"\n\n\n");
			   
		       //now find points with the closed path of one shape
		       //One shape has more than one close path if
		       // there is more than one <Ring> in ESRI data
			   
		       //Find all the <Ring> element 
		       //Each <Ring> is a close path 
		       Element current_shape_data = current_shape_value.getChild("RingArray");
			   
			   //Points are inside RingArray
		       //Its important to check as some data are directly inside <Value> Element
			   if(current_shape_data!=null)
			   {
			   //list all the <Ring> or closed paths
			   List Ring_list = current_shape_data.getChildren("Ring");
			   Iterator ring_iterator = Ring_list.iterator();
			   		
			   while(ring_iterator.hasNext())
			   		{
			   			
				        boolean isSegment =false;
				        Element current_shape_data_Ring = (Element) ring_iterator.next() ; 
			   
			   			//points are inside <Pointarray>
			   			Element current_shape_data_pointArray = current_shape_data_Ring.getChild("PointArray");
			   			
			   			//This a not a point array rather it is a segment array
			   			if(current_shape_data_pointArray==null)
			   				{
			   				Element current_shape_data_segmentArray = current_shape_data_Ring.getChild("SegmentArray");   
			   				//put segments in Points List if there is no point array rather there is segment array
			   				Points = current_shape_data_segmentArray.getChildren("Segment");
			   				isSegment=true;
			   				
			   				
			   				}
			   			   
			   			//Put points in Points vector if not segment
			   			 if(!isSegment)
			   				{
			   				Points = current_shape_data_pointArray.getChildren("Point");
			   				}
			   		
			   					 		   
			 			   Iterator point_iterator = Points.iterator();
			 			   boolean firstPoint = true;
			 			   
			 			   
			 			   
			 			   //Iterate via each Point/Segment Array	   
			 			   while(point_iterator.hasNext())
			 			   {
			 				   
			 				  //get one point or segment
			 				  Element point = (Element) point_iterator.next(); 
			 				  Element arc_center=null;
			 				  Element direction =null;
			 				  boolean ccw=false;
			 				  Element centerX=null;
			 				  int count=0;
			 				  Element lastpoint=null;
			 				  boolean isLastSegment =false;
			 				  //if it is segment then go to <FromPoint> Element inside the <Segment>
			 				   //Else it is inside <Point> Element
 			 				   if (isSegment) 
			 					   {
 			 					       if(!point_iterator.hasNext()) 
 			 					    	   {
 			 					    	    isLastSegment=true;
 			 					            //Reading the End point for last Segment
 			 					            lastpoint = point.getChild("ToPoint");
 			 					    	   }
 			 					       
 			 					       count=1;
 			 					       arc_center = point.getChild("CenterPoint");
 			 					       
			 					      
 			 					       	if (arc_center!=null) 
 			 						   	{
 			 					    	 centerX = arc_center.getChild("X");
 			 						    
 			 					    	 //there is some segment which has CenterPoint with no X Y value and says nil="true"
 			 					    	 	if(centerX!=null) 
 			 					    		{
 			 					    		count = 2;
 			 					    		}
 			 					    		direction = point.getChild("IsCounterClockwise");
 			 					    		ccw = direction.getText().equals("true");
 			 					    		
 			 					    	
 			 						   	}
 			 					     
 			 					     point= point.getChild("FromPoint");
			 					   
			 					   }
 			 				 
 			 				  
 			 				 int last=0;
 			 				 do{
 			 					 
			 				   //Pick up points 
 			 				   double x = Double.parseDouble(point.getChildText("X")); 
			 				   //changing the coordinate system 
 			 				   double y = Double.parseDouble(point.getChildText("Y")); 
 			 				   
 			 				   
 			 				 //if(count==3 && last!=1)
	 					  	  {
	 					  		  
	 					  		 // System.out.print("arc-> X is : " + x);
	 					  		  //System.out.print("Y is : " + y+"\n");  
	 					  		  
	 					  	  }
 			 				   
 			 				   
 			 				   		  x= (double)(Math.round((x-1324343.4)*10))/10;	
			 				   
 			 				    
			 					  	  y =(double)(Math.round((y-877552.8)*10))/10; 
			 					
			 					
			 					  	//if(p==1)
			 					  	  {
			 					  		  //System.out.println(point.toString());
			 					  		  //System.out.print("X is : " + x);
			 					  		  //System.out.print("Y is : " + y+" \n");  
			 					  		  
			 					  	  }
		 			 				   
			 					  	  
			 					  	  
			 					  	 if(first_x){
								  		  max_min_x[0] = x;
								  		  max_min_x[1] = x;
								  		  first_x = false;
								  }
								  else if(!first_x){
								  	 if(x < max_min_x[0]){
								  		  max_min_x[0] = x;
								  		 // System.out.println("Min X is : " + x);
								  	  }
								  	  else if(x > max_min_x[1]){
								  		  max_min_x[1] = x;
								  		  //System.out.println("Max x is : " + x);
								  	 }
								  }
							  	  
								  if(first_y){
								  	 max_min_y[0] = y;
								     max_min_y[1] = y;
								     first_y = false;
								  }
								  else if(!first_y){
								  	  	if(y < max_min_y[0]){
								  	  		max_min_y[0] = y;
								  			//System.out.println("Min Y is : " + y);
								  		}
								  		else if(y > max_min_y[1]){
								  			max_min_y[1] = y;
								  			//System.out.println("Max Y is : " + y);
								  		}
								 }
			 					  	  
			 					  	  
			 					
			 					  	  
			 				   //put points in GeoPoint Object
			 					GeoPoint gp = new GeoPoint(x, y);
			 				   		 				   
			 				   // if 1st point in this closed path,
			 					// mark it as a move point			   
			 				   if (firstPoint) 
			 				   	{
			 						gp.setMovePoint(true);
			 						firstPoint = false;
			 				   	
			 				   	}
			 				   
			 				  //set control point
			 				  if(arc_center!=null && count==3 && last!=1)
			 					  {
			 					  
			 					  gp.setControlPoint(ccw);
			 					  
			 					  }
			 				   
			 				   gShape.addPoint(gp); 			 				    
			 				   
			 				   if (count==2) 
			 				    {
			 				    
			 					   point=arc_center;
			 					 	
			 				    }
			 				   
			 				   else if(isLastSegment)
			 					   {
			 					   point=lastpoint;
			 					   last=1;
			 					   isLastSegment=false;
			 					   
			 					   //System.out.println("COUNT"+count);
			 					   continue;
			 					   }
			 				   
			 				   else 
			 					   break;
			 				   
			 				   
			 				    count++; 
			 				   	
			 						
 			 				   }while(count<=3 && isSegment);
 			 				   
			 			   }//all points added to one shape	   
			 			   
			 		   }//end of adding segment  or point
			 		
			   		}
			   
			   
			   //data is inside 2nd <Value> and its just a point 
			   //no closed path
			   if(current_shape_data==null)
			   {
				
				   //read directly from 2nd <Value> element
				   Element value = current_shape_value;
				   int numberOfEdges=12;
				   double arcLength=12.0;
				   
				   double theta = Math.toRadians(360.0 / numberOfEdges);
				   
				   double x = Double.parseDouble(value.getChildText("X"));
			   		  x= (double)(Math.round((x-1324343.4)*10))/10;	
			       
			   	   double y = Double.parseDouble(value.getChildText("Y"));
				  	  y =(double)(Math.round((y-877552.8)*10))/10;
				  	  
				  	  if(first_x){
					  		  max_min_x[0] = x;
					  		  max_min_x[1] = x;
					  		  first_x = false;
					  }
					  else if(!first_x){
					  	 if(x < max_min_x[0]){
					  		  max_min_x[0] = x;
					  		//System.out.println("Min X is : " + x);
					  	  }
					  	  else if(x > max_min_x[1]){
					  		  max_min_x[1] = x;
					  		//System.out.println("Max x is : " + x);
					  	 }
					  }
				  	  
					  if(first_y){
					  	 max_min_y[0] = y;
					     max_min_y[1] = y;
					     first_y = false;
					  }
					  else if(!first_y){
					  	  	if(y < max_min_y[0]){
					  	  		max_min_y[0] = y;
					  			//System.out.println("Min Y is : " + y);
					  		}
					  		else if(y > max_min_y[1]){
					  			max_min_y[1] = y;
					  			//System.out.println("Max Y is : " + y);
					  		}
					 }
			
				  //made additional 12 points to add in the GeoShape to look the shape noticeable	
				   
				   for (int j = 0; j < numberOfEdges; j++) 
				   {
						double radians = theta * j;
						double xTemp = x + (Math.cos(radians) * arcLength);
						double yTemp = y + (Math.sin(radians) * arcLength);
						GeoPoint gp = new GeoPoint(xTemp, yTemp);
						// add the point to the GeoShape
						gShape.addPoint(gp);
					}
				   
			
			   }
			  //break used breaking the loop when we got the second <Value> Element
			   break;
		   } 
		   
		   } 
		   
		   
		   
			   if (gShape.getPointCount() > 0) {
					gShape.setColor(color);
					gShape.finalizeShape();   /* Added finalizeShape call */
					shapes.add(gShape);
				}
		   
			   
		   
		   }//end of one <RingArray> might have several <Ring> that indicate particular shape 
		   
		   geoShapes = new GeoShape[shapes.size()];
			shapes.copyInto(geoShapes); 
		   
         
	}
	
	
			
	
	
	
	
	public void readGISFile()  {
		
		Element records;
		
		try{	 
			   
			   //building xml document	
			   SAXBuilder dbuilder =new SAXBuilder();
			      
			   Document doc=dbuilder.build(fileName);
			   //getting the whole dataset from <Records> from ESRI GIS XML
			   records = (Element)XPath.selectSingleNode(doc, "/esri:Workspace/WorkspaceData/DatasetData/Data/Records");
			   //go for reading shapes
			   readShapes(records , "FF00FF");
			   
		    }
				
		catch(JDOMException e)
			{
					
			}
		
		catch(IOException e)
			{
				
			}
	
	}
	
	public void applyStyles(StyleSheetReader styleReader) {
		if (geoShapes != null && geoShapes.length > 0) {
			// if styles are available apply them to the shapes
			styleReader.applyStyles(geoShapes);
		}
		
	}
	
	
	public static void main(String[] args)  {
		if (args.length != 2) {
			System.err
					.println("Usage: java GisXmlReader <xml filename or directory> <stylesheet xml file>");
			System.exit(1);
		}

		String styleFile = "";
		try {
			styleFile = args[1];
		} catch (Exception e) {
			e.printStackTrace();
		}

		// read in the style sheet information to be applied
		// to the GIS xml data
		StyleSheetReader styleReader = new StyleSheetReader();
		boolean stylesAvailable = styleReader.readStyles(styleFile);

		String fileName = "";
		try {
			fileName = args[0];
		} catch (Exception e) {
			e.printStackTrace();
		}

		// holds all the files to be processed
		File[] xmlFiles;

		// If the input file is a directory then get all the files in
		// the directory, otherwise just put the file in the array
		File inputFiles = new File(fileName);
		if (inputFiles.isDirectory()) {
			xmlFiles = inputFiles.listFiles();
		} else {
			xmlFiles = new File[1];
			xmlFiles[0] = inputFiles;
		}

		// go through all the files in the array and process any,
		// ending with .xml
		for (int i = 0; i < xmlFiles.length; i++) {
			String file = xmlFiles[i].getAbsolutePath();
			if (file.endsWith(".xml")) {
				GisXmlReader xmlReader = new GisXmlReader(file);
				
				
			}
		}
	}
}

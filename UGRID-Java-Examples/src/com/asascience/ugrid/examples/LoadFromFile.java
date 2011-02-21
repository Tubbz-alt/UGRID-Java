package com.asascience.ugrid.examples;

import java.io.IOException;
import java.util.Formatter;
import java.util.List;
import ucar.nc2.Variable;
import ucar.nc2.constants.FeatureType;
import ucar.nc2.dt.UGridDataset;
import ucar.nc2.dt.ugrid.Cell;
import ucar.nc2.dt.ugrid.Entity;
import ucar.nc2.dt.ugrid.Mesh;
import ucar.nc2.dt.ugrid.geom.LatLonPoint2D;
import ucar.nc2.ft.FeatureDatasetFactoryManager;
import ucar.nc2.util.CancelTask;
import ucar.unidata.geoloc.LatLonPoint;
import ucar.unidata.geoloc.LatLonPointImpl;
import ucar.unidata.geoloc.LatLonRect;
/**
 *
 * @author Kyle
 */
public class LoadFromFile {
public static void main(String[] args) {
      CancelTask cancelTask = null;
      //String unstructured = "C:/Dev/Unstructured/ugrid/FVCOM/fvcom_delt.ncml";
      //String unstructured = "C:/Dev/Unstructured/ugrid/ELCIRC/elcirc_delt.ncml";
      //String unstructured = "C:/Dev/Unstructured/ugrid/ADCIRC/adcirc_delt.ncml";
      String unstructured = "C:/Dev/Unstructured/ugrid/SELFE/selfe_delt.ncml";
      try {
        UGridDataset ugrid = (UGridDataset)FeatureDatasetFactoryManager.open(FeatureType.UGRID, unstructured, cancelTask, new Formatter());
        long startTime;
        long endTime;
        for (Mesh m : ugrid.getUGrids()) {
          System.out.println(m.getName());
          System.out.println("Mesh contains: " + m.getSize() + " cells (polygons).");
          System.out.println("Mesh contains: " + m.getNodeSize() + " nodes.");
          System.out.println("Mesh contains: " + m.getEdgeSize() + " edges.");
          System.out.println("Mesh contains: " + m.getFaceSize() + " faces.");
          // We build now, to see how grids compare in index time
          startTime = System.currentTimeMillis();
          m.buildRTree();
          endTime = System.currentTimeMillis();
          System.out.println("RTree build took: " + (double)(endTime - startTime)/1000 + " seconds.");
          System.out.println("RTree contains: " + m.getTreeSize() + " entries.");

          if (m.getTreeSize() > 0) {
            // Query a random point within the bounding box of the Mesh
            LatLonRect bounds = m.getLatLonBoundingBox();
            double query_lat = bounds.getLatMin() + (Math.random() * (bounds.getLatMax()-bounds.getLatMin()));
            double query_lon = bounds.getLonMin() + (Math.random() * (bounds.getLonMax()-bounds.getLonMin()));
            LatLonPoint query_point = new LatLonPointImpl(query_lat, query_lon);
            System.out.println("Random query point: " + query_lat + "," + query_lon);

            // Get the Cell that the point is in
            Cell cell = m.getCellFromLatLon(query_lat,query_lon);
            System.out.println("Cell containing point located.");

            List<LatLonPoint2D> vertices = cell.getPolygon().getVertices();
            System.out.println("Cell vertices (" + vertices.size() + "):");
            for (LatLonPoint2D p : vertices) {
              System.out.println(p.getLatitude() + "," + p.getLongitude());
            }
            System.out.println("Cell center: " + cell.getPolygon().getCentroid().getLatitude() + "," + cell.getPolygon().getCentroid().getLongitude());

            // The Cell contains many Entities, which are points which data
            // can lay on.  Can test if the Entity is on the boundry.
            System.out.println("Data locations within the Cell:");
            for (Entity e : cell.getEntities()) {
              System.out.println(e.getClass() + ": " + e.getGeoPoint().getLatitude() + "," + e.getGeoPoint().getLongitude());
            }


            for (Variable v : m.getVariables()) {
              // Extract value for each variable at the query point
              System.out.print(v.getName() + ": ");
              System.out.print(m.extractPointData(v,query_point));
              System.out.println();
            }
          }
        }
      } catch (IOException e) {
        System.out.println(e);
      }
    }

}

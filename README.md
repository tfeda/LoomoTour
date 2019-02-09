# LoomoTour

### Purpose
The purpose of this project is apart of the UW-Makerspace tech challenge of programming Loomo 
to give a tour of the facility. That being said, It can easily be reused for any application
that uses Loomo's navigation and Speech in a linear manner.

### Usage
LoomoTour operates by parsing a csv file and creates a series of nodes along the tour. Each node
contains the name of the node, a description that is read out loud by Loomo, as well as a list of x and y locations (in meters) relative
to Loomo's origin. The order goes in **Name, Description, xloc, yloc**. An example of this format is displayed below:

![Image view of the csv file](https://github.com/tfeda/LoomoTour/blob/master/README_images/Eample_csv.JPG "example.csv")

After the file is made, place it in **LoomoTour/src/main/res/raw/**. Then edit the method **setupTour** in **TourControl.java**

![TourControl](https://github.com/tfeda/LoomoTour/blob/master/README_images/Tour_control_edit.JPG "TourControl.java")

### In Progess
This is a work in progress program and a number of features are being implemented and improved upon. Namely:
* An end-of-tour directive
* More precise navigation
* Q/A ability at any given point of the tour

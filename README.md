# LoomoTour

### Purpose
The purpose of this project is apart of the UW-Makerspace tech challenge of programming Loomo 
to give a tour of the facility. That being said, It can easily be reused for any application
that uses Loomo's navigation and Speech in a linear manner.

### Usage
LoomoTour operates by parsing a csv file and creates a series of nodes along the tour. Each node
contains the name of the node, a description that is read out loud by Loomo, as well as a list of x and y locations (in meters) relative
to Loomo's origin. The order goes in **Name, Description, xloc, yloc**.

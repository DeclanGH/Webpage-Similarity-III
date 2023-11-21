# Webpage-Similarity-III
============

This extends the Webpage-Similarity-II project to record links from each site to its neighbors, for at least 1000 total sites (We would be ignoring Wikipedia navigation links here). The edges along with their similarity metrics are then stored persistently in a Serialized file.
The program uses a GUI that recreates the graph from the above, and reports the number of disjoint sets as a connectivity check. The user is allowed to select any two sites, and display the shortest (with resepect to simiiarity weights) path between them. The path would be indicated by a series of sites.

Professor: [Doug Lea](http://gee.cs.oswego.edu/dl)

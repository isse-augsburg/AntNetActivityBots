# A Real-Word Realization of the AntNet Routing Algorithm with ActivityBots

Real World Implementation of Ant-Colonization-Optimization (ACO) using Paralax Activity Bots

## Abstract

Moving from a theoretical implementation only using simulations to a real-world implementation there are many challenges that need to be addressed. In this video we present a real-world implementation of the AntNet Routing Algorithm using a fleet of educational Robots called ActivityBots [1] produced by Parallax.
It utilizes the Ant Colony Optimization (ACO)[2] algorithm by Prof. Dr. Marco Dorigo to find a short path. 
While traversing the graph, the individual ants leave behind a trail of pheromones when successfully reaching the goal. 
In turn other ants can use a heuristic over the distance and the pheromones to choose which exit to select when at a node. 
Following this procedure, the ants converge to a single short path, however an optimal path can’t be guaranteed.

As robotic ants don’t have a way of leaving behind pheromones nor have shared knowledge, we equipped each ant with an WiFi-chip to collect the information on a single server. This server also evaluates the heuristic at the node to allow simple changes of parameters without having to repeatedly reprogram the mobile Robots while staying true to biology and the algorithm. Each Robot is equipped with a line follower sensor at the bottom with a resolution of 8 bit spanning the width of the robot. A node in the graph is represented by an intersection which has the ID and entry number encoded in barcodes at the entry and exit. Connections between the nodes are built using several bidirectional road segments to prevent collisions of approaching vehicles. Due to the limited number of real ants, the algorithm can be speed up by supporting it with further simulated ants.

Purpose of the development of this project was to support the education of swarm algorithms like ACO by visualizing them in the real word. Only seeing simulations on a screen is often dull. Using our centralized approach students can implement new swarm algorithms by only changing code on the server and instantly see their results on the ActivityBots leading to a more motivating environment.

[1] ActivityBot 360° Robot Kit by Parallax Inc. Online. https://www.parallax.com/product/activitybot-360-robot-kit/ [Accessed on 2021-04-13]  
[2] M. Dorigo, T. Stützle: Ant Colony Optimization. MIT Press / Bradford Books, Cambridge MA 2004, ISBN 0-262-04219-3.

## Important Links
### Video Source
View a Demo of this Project [on YouTube](https://www.youtube.com/watch?v=7R8DzrirUuw).

### SourceCode 
Access the source code [here]( https://github.com/isse-augsburg/AntNetActivityBots).

## Project structure
### [C-Project](c_project)
- Source code of the C++ project running on the ActivityBots
- uses the Library PopWare
- Follows the lines on the floor and waits for commands from the PC

### [Routing Table](routingtable)
- Source Code of the Kotlin/Java Project running on the PC
- Manages communication between Bots and PC
- Executes the Algorithm and sends instructions to the individual Bots

### [Barcode Encoder](codeencoder)
- Helper project to automatically generate printable roundabout segments
- Encodes the given node ID into a barcode and saves it into a svg or pdf file  
- InkScape must be installed for the conversion from svg to pdf

### [Ant Shield](antshield)
- 3d-printable shield for improving the performance of the LineFollower
- Blocks incoming light from the sides and therefore improves the detection rate of the black lines
- Available as Creo Part, Step and STL  

### [Printables](printables)
- This folder contains all printable rail-segments that make up the map the Ants drive on
- The segments have to printed on A3 Paper, cut to size, optionally they can be laminated to improve longevity
- Additionally, a printable calibration strip is given to check how well the LineFollower is working
- The individual sensors should only kick on in the center of the strips, otherwise it has to be recalibrated
- A representation of the map shown in the demo video can be found in the file [2013Map.xlsx](printables/2013Map.xlsx)

### [Documentation](docs)
- Further documentation
- [Overview of the code with project structure as well as relevant code segments](docs/Code%20Overview.pdf)
- [Assembly instructions of the ActivityBots hardware](docs/Assembly%20Instructions%20Ants.pdf)

----
## Contact
Jonas Wilfert -- jonas.wilfert@uni-a.de  
Niklas Paprotta -- niklas.paprotta@uni-a.de  
Oliver Kosak -- kosak@isse.de  
Simon Stieber -- stieber@isse.de  

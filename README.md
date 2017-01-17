Jigsaw
======


Solve image jigsaws.


License
-------

AGPL 3. See LICENSE file.


Build
-----

Download a Java 8 JDK and Apache Maven >3.0.

    $ mvn clean install -DskipTests
    
Execute
-------

Execute with 

    $ java -jar target/jigsaw-1-SNAPSHOT.jar [input png] [output png]
    
Input png is the png file which is scrambled into rectangular tiles with the tiles being mixed up. Output png is the file where the "solved" image should be written to.

Note that this can take a considerable amount of time.
    
How it works
------------

Jigsaw works by first applying a Canny edge detection algorithm onto the input image (algorithm is made up of transforming to greyscale, a Gaussian blur, applying a Sobel filter, and then thinning out the edges using non-maximum supression and tracking the edges using a double threshold).

The edge image is then inspected and possible cut locations are tried to be found along the x nd y axis. Unlikely cut locations (according to the edges orthogonal to the cuts) are discarded. For all other cut location combinations, the jigsaw is solved.

Since solving tehe jigsaw is in O(n!), the amount of variants that are "tried" are thinned by inspecting the boarders of the tiles (of each possible cut) and are matched based on a BitSet to all other borders, then the most likely options survive and the others are discarded. The result of this step is a graph with tiles as vertices and potential matches to the tile (left/right/top/bottom) being edges. 

As finding best solutions on such a graph is still very expensive, the amount of compared solutions is reduced from O(n!) to O(2^k) with k being the number of tiles that need to be put next to each other horizontally. This is done by implementing the Color-Coding algorithm. Unfortunately, this is still pretty slow, so go fetch a cup of coffee while this is being executed. As a result of the Color-Coding, vertices in the graph are found which have at least k transitive neighbours to the right - and these vertices/tiles are therefore the ones that /might/ be placed on the very left border of the result image. All these options are then evaluated (which again takes quite some time).
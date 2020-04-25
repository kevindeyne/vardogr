# bclone
![Java CI with Maven](https://github.com/kevindeyne/data-scrambler/workflows/Java%20CI%20with%20Maven/badge.svg)

Realistic test data in development and qa environments can pinpoint bugs and performance issues early. However taking direct copies violates the security of data and takes time. It also does not scale. 

Bclone is a tool that can push production-like data to test databases securely. It does this by generating a distribution model of the data first - describing the data and its relative distribution. 

It can then run this model and generate data from it, either directly matching the origin size or scaling up. 

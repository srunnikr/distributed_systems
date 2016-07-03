#!/bin/bash

echo "-----------------------------------------------"

# test the hadoop cluster by running wordcount
HADOOP_HOME="/usr/local/hadoop"
mv /tmp/BigramCount.java $HADOOP_HOME/BigramCount.java
mv /tmp/process_bigrams.py $HADOOP_HOME/process_bigrams.py
cd $HADOOP_HOME

echo "---------------COMPILING JAVA------------------"

bin/hadoop com.sun.tools.javac.Main BigramCount.java
jar cf bc.jar BigramCount*.class

echo "--------------COPYING TO HDFS------------------"

# create input directory on HDFS
bin/hadoop fs -mkdir -p input

# put input files to HDFS
bin/hdfs dfs -put ./input/* input

echo "-------------RUNNING MAP-REDUCE----------------"
echo "-----------------------------------------------"

# run wordcount 
bin/hadoop jar bc.jar BigramCount input output

bin/hdfs dfs -get output/part-r-00000 $HADOOP_HOME/output.txt

echo "-----------------------------------------------"
echo "--------------------OUTPUT---------------------"
echo "-----------------------------------------------"

python process_bigrams.py output.txt

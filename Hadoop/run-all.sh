# Start containers
docker stop slave1
docker rm slave1

docker run \
	-itd \
	--name=slave1 \
	sequenceiq/hadoop-docker:2.7.0 \
	/etc/bootstrap.sh -bash

docker stop slave2
docker rm slave2

docker run \
	-itd \
	--name=slave2 \
	sequenceiq/hadoop-docker:2.7.0 \
	/etc/bootstrap.sh -bash

docker stop master
docker rm master

docker run \
	-itd \
	--name=master \
	--link=slave1:slave1 \
	--link=slave2:slave2 \
	sequenceiq/hadoop-docker:2.7.0 \
	/etc/bootstrap.sh -bash

docker exec -itd master "echo \"slave1\nslave2\"  >> $HADOOP_PREFIX/etc/hadoop/slaves"

#stopping stuff
docker exec -itd master "$HADOOP_PREFIX/sbin/stop-dfs.sh"
docker exec -itd master "$HADOOP_PREFIX/sbin/stop-yarn.sh"
docker exec -itd slave1 "$HADOOP_PREFIX/sbin/stop-dfs.sh"
docker exec -itd slave1 "$HADOOP_PREFIX/sbin/stop-yarn.sh"
docker exec -itd slave2 "$HADOOP_PREFIX/sbin/stop-dfs.sh"
docker exec -itd slave2 "$HADOOP_PREFIX/sbin/stop-yarn.sh"

#starting stuff
docker exec -itd master "echo "Y" | $HADOOP_PREFIX/bin/hdfs namenode -format awesome"
docker exec -itd master "$HADOOP_PREFIX/sbin/hadoop-daemon.sh --config $HADOOP_CONF_DIR --script hdfs start namenode"
docker exec -itd slave1 "$HADOOP_PREFIX/sbin/hadoop-daemons.sh --config $HADOOP_CONF_DIR --script hdfs start datanode"
docker exec -itd slave2 "$HADOOP_PREFIX/sbin/hadoop-daemons.sh --config $HADOOP_CONF_DIR --script hdfs start datanode"

docker exec -itd master "$HADOOP_PREFIX/sbin/start-dfs.sh"
docker exec -itd master "$HADOOP_YARN_HOME/sbin/yarn-daemon.sh --config $HADOOP_CONF_DIR start resourcemanager"

docker exec -itd master "$HADOOP_YARN_HOME/sbin/yarn-daemons.sh --config $HADOOP_CONF_DIR start nodemanager"
docker exec -itd slave1 "$HADOOP_YARN_HOME/sbin/yarn-daemons.sh --config $HADOOP_CONF_DIR start nodemanager"
docker exec -itd slave2 "$HADOOP_YARN_HOME/sbin/yarn-daemons.sh --config $HADOOP_CONF_DIR start nodemanager"

docker exec -itd master "$HADOOP_YARN_HOME/sbin/yarn-daemon.sh --config $HADOOP_CONF_DIR start proxyserver"
docker exec -itd master "$HADOOP_PREFIX/sbin/start-yarn.sh"

docker exec -itd master "$HADOOP_PREFIX/sbin/mr-jobhistory-daemon.sh --config $HADOOP_CONF_DIR start historyserver"

docker exec -it master bash

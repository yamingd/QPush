package com.argo.qpush.core;

import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import redis.clients.jedis.BinaryJedis;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Created with IntelliJ IDEA.
 * User: Yaming
 * Date: 2014/10/3
 * Time: 20:36
 */
public class BinaryJedisFactory implements PooledObjectFactory<BinaryJedis> {
    
    private final AtomicReference<HostAndPort> hostAndPort = new AtomicReference<HostAndPort>();
    private final int timeout;
    private final String password;
    private final int database;
    private final String clientName;

    public BinaryJedisFactory(final String host, final int port, final int timeout,
                              final String password, final int database) {
        this(host, port, timeout, password, database, null);
    }

    public BinaryJedisFactory(final String host, final int port, final int timeout,
                              final String password, final int database, final String clientName) {
        super();
        this.hostAndPort.set(new HostAndPort(host, port));
        this.timeout = timeout;
        this.password = password;
        this.database = database;
        this.clientName = clientName;
    }

    public void setHostAndPort(final HostAndPort hostAndPort) {
        this.hostAndPort.set(hostAndPort);
    }

    @Override
    public void activateObject(PooledObject<BinaryJedis> pooledJedis)
            throws Exception {
        final BinaryJedis jedis = pooledJedis.getObject();
        if (jedis.getDB() != database) {
            jedis.select(database);
        }

    }

    @Override
    public void destroyObject(PooledObject<BinaryJedis> pooledJedis) throws Exception {
        final BinaryJedis jedis = pooledJedis.getObject();
        if (jedis.isConnected()) {
            try {
                try {
                    jedis.quit();
                } catch (Exception e) {
                }
                jedis.disconnect();
            } catch (Exception e) {

            }
        }

    }

    @Override
    public PooledObject<BinaryJedis> makeObject() throws Exception {
        final HostAndPort hostAndPort = this.hostAndPort.get();
        final Jedis jedis = new Jedis(hostAndPort.getHost(), hostAndPort.getPort(), this.timeout);

        jedis.connect();
        if (null != this.password) {
            jedis.auth(this.password);
        }
        if (database != 0) {
            jedis.select(database);
        }
        if (clientName != null) {
            jedis.clientSetname(clientName);
        }

        return new DefaultPooledObject<BinaryJedis>(jedis);
    }

    @Override
    public void passivateObject(PooledObject<BinaryJedis> pooledJedis)
            throws Exception {
        // TODO maybe should select db 0? Not sure right now.
    }

    @Override
    public boolean validateObject(PooledObject<BinaryJedis> pooledJedis) {
        final BinaryJedis jedis = pooledJedis.getObject();
        try {
            HostAndPort hostAndPort = this.hostAndPort.get();

            String connectionHost = jedis.getClient().getHost();
            int connectionPort = jedis.getClient().getPort();

            return hostAndPort.getHost().equals(connectionHost) && hostAndPort.getPort() == connectionPort &&
                    jedis.isConnected() && jedis.ping().equals("PONG");
        } catch (final Exception e) {
            return false;
        }
    }
}

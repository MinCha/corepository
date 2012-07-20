package com.github.corepo.client;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import org.junit.Ignore;
import org.junit.Test;

import tokyotyrant.MRDB;
import tokyotyrant.networking.NodeAddress;
import tokyotyrant.transcoder.StringTranscoder;

/**
 * You need running TT server for executing this test because this is an
 * integration test with TT.
 * 
 * @author Min Cha
 * 
 */
public class TTCoRepositoryIntegrationTest extends CoRepositoryAcceptanceTest {
	private final String ip = "10.64.176.139"; // change this to your ip before executing
	private final int port = 1978; // change this to your port before executing
	private MRDB tt;

	@Override
	protected CoRepository getCoRepository() throws Exception {
		tt = new MRDB();
		tt.setGlobalTimeout(2000);
		tt.open(NodeAddress.addresses("tcp://" + ip + ":" + port));

		return new TTCoRepository(tt);
	}

	@Ignore
	@Test
	public void canRunExtOperationWithLockingOption() throws Exception {
		assertThat(tt.await(tt.ext("tcrdbput", key, "1", 1,
				new StringTranscoder())), is(notNullValue()));
		assertThat(tt.await(tt.get(key)), is(notNullValue()));
	}
}

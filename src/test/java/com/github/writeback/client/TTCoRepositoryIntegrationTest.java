package com.github.writeback.client;

import tokyotyrant.MRDB;
import tokyotyrant.networking.NodeAddress;

/**
 * You need running TT server for executing this test because this is an integration test with TT.
 * 
 * @author Min Cha
 *
 */
public class TTCoRepositoryIntegrationTest extends CoRepositoryAcceptanceTest {
	private final String ip = "yourTTServerIp"; // change this before executing
	private final int port = 1978; // change this before executing
	
	@Override
	protected CoRepository getCoRepository() throws Exception {
		MRDB tt = new MRDB();
		tt.setGlobalTimeout(1000);
		tt.open(NodeAddress.addresses("tcp://" + ip + ":" + port));
		return new TTCoRepository(tt);
	}
}

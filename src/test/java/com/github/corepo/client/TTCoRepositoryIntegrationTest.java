package com.github.corepo.client;

import com.github.corepo.client.CoRepository;
import com.github.corepo.client.TTCoRepository;

import tokyotyrant.MRDB;
import tokyotyrant.networking.NodeAddress;

/**
 * You need running TT server for executing this test because this is an
 * integration test with TT.
 * 
 * @author Min Cha
 * 
 */
public class TTCoRepositoryIntegrationTest extends CoRepositoryAcceptanceTest {
	private final String ip = "10.64.169.238"; // change this to your ip before executing
	private final int port = 1978; // change this to your port before executing

	@Override
	protected CoRepository getCoRepository() throws Exception {
		MRDB tt = new MRDB();
		tt.setGlobalTimeout(2000);
		tt.open(NodeAddress.addresses("tcp://" + ip + ":" + port));
		return new TTCoRepository(tt);
	}
}

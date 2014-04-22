package com.github.corepo.client;

import java.util.Arrays;

public class Writeback {
    private CoRepository coRepository;
    private OriginalRepository originalRepository;

    public Writeback(CoRepository coRepository,
	    OriginalRepository originalRepository) {
	this.coRepository = coRepository;
	this.originalRepository = originalRepository;
    }

    public void writeback(String key) {
	if (coRepository.exists(key) == false) {
	    return;
	}

	if (coRepository.isInt(key)) {
	    originalRepository.writeback(Arrays.asList(coRepository
		    .selectAsInt(key)));
	} else {
	    originalRepository.writeback(Arrays.asList(coRepository
		    .selectAsObject(key)));
	}
    }
}

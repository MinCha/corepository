package com.github.corepo.client.integration;

import com.github.corepo.client.CoRepositoryClient;
import com.github.corepo.client.Item;
import com.github.corepo.client.LocalMemoryCoRepository;
import com.github.corepo.client.OriginalRepository;
import com.github.corepo.client.measurement.support.VisitationDAO;
import com.github.corepo.client.measurement.support.VisitationOriginalRepository;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class CoRepositoryEndToEndTest {
    private final String key = "count";

    @Test
    public void oneUserUsesCoRepository() {
        OriginalRepository originalRepository = new VisitationOriginalRepository(
                new VisitationDAO());
        CoRepositoryClient client = new CoRepositoryClient(
                new LocalMemoryCoRepository(), originalRepository, 10000);

        int count = client.selectAsInt(key).getValueAsInt();
        count++;
        count++;
        client.update(new Item(key, count));

        assertThat(client.selectAsInt(key).getValueAsInt(), is(2));
        assertThat(originalRepository.read(key).getValueAsInt(), is(0));
    }
}

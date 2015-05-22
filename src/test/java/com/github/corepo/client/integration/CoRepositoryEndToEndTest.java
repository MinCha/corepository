package com.github.corepo.client.integration;

import com.github.corepo.client.*;
import com.github.corepo.client.measurement.support.VisitationDAO;
import com.github.corepo.client.measurement.support.VisitationOriginalRepository;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class CoRepositoryEndToEndTest {
    private final ItemKey key = new ItemKey("count");

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

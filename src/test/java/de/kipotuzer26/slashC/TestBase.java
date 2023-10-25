package de.kipotuzer26.slashC;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import net.milkbowl.vault.economy.Economy;
import de.kipotuzer26.slashC.integrations.vault.VaultProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import static org.mockito.Mockito.mock;

public abstract class TestBase {

    protected ServerMock server;
    protected Main plugin;
    protected Economy economy;

    @BeforeEach
    public void setUp() {
        server = MockBukkit.mock();
        plugin = MockBukkit.load(Main.class);
        mockVaultEconomy();
    }

    private void mockVaultEconomy() {
        economy = mock(Economy.class);
        plugin.setVault(new VaultProvider(economy));
    }

    @AfterEach
    public void tearDown() {
        MockBukkit.unmock();
    }
}

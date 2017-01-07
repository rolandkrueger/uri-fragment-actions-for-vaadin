package org.vaadin.uriactions.testhelpers;

import com.vaadin.navigator.NavigationStateManager;
import com.vaadin.navigator.Navigator;

public class TestNavigationStateHandler implements NavigationStateManager {
    private String state;

    @Override
    public String getState() {
        return state;
    }

    @Override
    public void setState(final String state) {
        this.state = state;
    }

    @Override
    public void setNavigator(final Navigator navigator) {
    }
}

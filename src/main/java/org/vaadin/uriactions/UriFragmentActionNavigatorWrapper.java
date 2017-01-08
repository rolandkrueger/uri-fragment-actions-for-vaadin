package org.vaadin.uriactions;

import com.google.gwt.thirdparty.guava.common.base.Preconditions;
import com.vaadin.navigator.NavigationStateManager;
import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.Navigator.ComponentContainerViewDisplay;
import com.vaadin.navigator.Navigator.SingleComponentContainerViewDisplay;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.navigator.ViewDisplay;
import com.vaadin.navigator.ViewProvider;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.SingleComponentContainer;
import com.vaadin.ui.UI;
import org.roklib.urifragmentrouting.UriActionCommand;
import org.roklib.urifragmentrouting.UriActionMapperTree;

public class UriFragmentActionNavigatorWrapper {
    private final Navigator navigator;
    private UriActionMapperTree uriActionMapperTree;
    private UriActionCommand currentActionCommandObject;
    private Object routingContext;

    public UriFragmentActionNavigatorWrapper(final UI ui) {
        this(ui, null);
    }

    public UriFragmentActionNavigatorWrapper(final UI ui, final NavigationStateManager navigationStateManager) {
        this(ui, navigationStateManager, (ViewDisplay) null);
    }

    public UriFragmentActionNavigatorWrapper(final UI ui, final NavigationStateManager navigationStateManager, final ComponentContainer container) {
        this(ui, navigationStateManager, new ComponentContainerViewDisplay(container));
    }

    public UriFragmentActionNavigatorWrapper(final UI ui, final NavigationStateManager navigationStateManager, final SingleComponentContainer container) {
        this(ui, navigationStateManager, new SingleComponentContainerViewDisplay(container));
    }

    public UriFragmentActionNavigatorWrapper(final UI ui, final NavigationStateManager navigationStateManager, final ViewDisplay viewDisplay) {
        final UriActionViewDisplay uriActionViewDisplay = new UriActionViewDisplay(viewDisplay);

        if (navigationStateManager != null) {
            navigator = new Navigator(ui, navigationStateManager, uriActionViewDisplay);
        } else {
            navigator = new Navigator(ui, uriActionViewDisplay);
        }
        navigator.addProvider(new UriActionViewProvider());
    }

    public Navigator getNavigator() {
        return navigator;
    }

    public void setUriActionMapperTree(final UriActionMapperTree dispatcher) {
        uriActionMapperTree = dispatcher;
    }

    public void setRoutingContext(final Object routingContext) {
        this.routingContext = routingContext;
    }

    private class UriActionViewDisplay implements ViewDisplay {
        private final ViewDisplay userProvidedDisplay;

        public UriActionViewDisplay(final ViewDisplay userProvidedDisplay) {
            this.userProvidedDisplay = userProvidedDisplay;
        }

        @Override
        public void showView(final View view) {
            if (userProvidedDisplay != null && !(view instanceof ActionExecutionView)) {
                userProvidedDisplay.showView(view);
            }
            // Otherwise there is nothing to do in this case. Action command is executed in ActionExecutionView.enter().
        }
    }

    private class UriActionViewProvider implements ViewProvider {
        @Override
        public String getViewName(final String viewAndParameters) {
            if (uriActionMapperTree == null) {
                return null;
            }
            final UriActionCommand action = uriActionMapperTree.interpretFragment(viewAndParameters, routingContext, false);
            if (currentActionCommandObject != null) {
                throw new IllegalStateException(
                        "Thread synchronization problem: this action navigator is currently handling another request. Current action is: "
                                + currentActionCommandObject);
            }
            currentActionCommandObject = action;
            return action != null ? viewAndParameters : null;
        }

        @Override
        public View getView(final String viewName) {
            return new ActionExecutionView(currentActionCommandObject);
        }
    }

    public class ActionExecutionView implements View {
        private final UriActionCommand command;

        ActionExecutionView(final UriActionCommand command) {
            Preconditions.checkNotNull(command);
            this.command = command;
        }

        @Override
        public void enter(final ViewChangeEvent event) {
            command.run();
            currentActionCommandObject = null;
        }

        public UriActionCommand getUriActionCommand() {
            return command;
        }
    }
}

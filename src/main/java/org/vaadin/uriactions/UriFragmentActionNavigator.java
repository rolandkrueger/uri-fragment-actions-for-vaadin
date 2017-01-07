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

public class UriFragmentActionNavigator {
    private final Navigator navigator;
    private UriActionMapperTree uriActionMapperTree;
    private UriActionCommand currentAction;
    private String currentViewAndParameters;

    public UriFragmentActionNavigator(final UI ui) {
        this(ui, null);
    }

    public UriFragmentActionNavigator(final UI ui, final NavigationStateManager navigationStateManager) {
        this(ui, navigationStateManager, (ViewDisplay) null);
    }

    public UriFragmentActionNavigator(final UI ui, final NavigationStateManager navigationStateManager, final ComponentContainer container) {
        this(ui, navigationStateManager, new ComponentContainerViewDisplay(container));
    }

    public UriFragmentActionNavigator(final UI ui, final NavigationStateManager navigationStateManager, final SingleComponentContainer container) {
        this(ui, navigationStateManager, new SingleComponentContainerViewDisplay(container));
    }

    public UriFragmentActionNavigator(final UI ui, final NavigationStateManager navigationStateManager, final ViewDisplay viewDisplay) {
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

    private void resetCurrentAction() {
        currentAction = null;
    }

    public void replay() {
        navigator.navigateTo(currentViewAndParameters);
    }

    public String getCurrentlyHandledUriFragment() {
        return currentViewAndParameters;
    }

    public boolean hasUriActionDispatcher() {
        return uriActionMapperTree != null;
    }

    public UriActionMapperTree getUriActionMapperTree() {
        return uriActionMapperTree;
    }

    public void setUriActionMapperTree(final UriActionMapperTree dispatcher) {
        uriActionMapperTree = dispatcher;
    }

    private void checkUriActionMapperTree() {
        if (!hasUriActionDispatcher()) {
            throw new IllegalStateException("No URI action mapper tree has been set for this object yet.");
        }
    }

    private class UriActionViewDisplay implements ViewDisplay {
        private final ViewDisplay userProvidedDisplay;

        public UriActionViewDisplay(final ViewDisplay userProvidedDisplay) {
            this.userProvidedDisplay = userProvidedDisplay;
        }

        @Override
        public void showView(final View view) {
            if (view instanceof ActionExecutionView) {
                // Nothing to do in this case. Action command is executed in ActionExecutionView.enter().
            } else if (userProvidedDisplay != null) {
                userProvidedDisplay.showView(view);
            }
        }
    }

    private class UriActionViewProvider implements ViewProvider {
        @Override
        public String getViewName(final String viewAndParameters) {
            checkUriActionMapperTree();
            final UriActionCommand action = uriActionMapperTree.interpretFragment(viewAndParameters);
            if (currentAction != null) {
                throw new IllegalStateException(
                        "Thread synchronization problem: this action navigator is currently handling another request. Current action is: "
                                + currentAction);
            }
            currentAction = action;
            if (action != null) {
                currentViewAndParameters = viewAndParameters;
                return viewAndParameters;
            } else {
                currentViewAndParameters = null;
                return null;
            }
        }

        @Override
        public View getView(final String viewName) {
            return new ActionExecutionView(currentAction);
        }
    }

    private class ActionExecutionView implements View {
        private final UriActionCommand command;

        public ActionExecutionView(final UriActionCommand command) {
            Preconditions.checkNotNull(command);
            this.command = command;
        }

        @Override
        public void enter(final ViewChangeEvent event) {
            resetCurrentAction();
            command.run(); // TODO: command has already been executed
        }

        public UriActionCommand getUriActionCommand() {
            return command;
        }
    }
}

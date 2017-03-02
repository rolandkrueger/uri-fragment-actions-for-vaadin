package org.vaadin.uriactions;

import com.google.gwt.thirdparty.guava.common.base.Preconditions;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import org.roklib.urifragmentrouting.UriActionCommand;

/**
 * Internally used view which is responsible for executing the {@link UriActionCommand} objects which will be created
 * for a URI fragment when this fragment could be resolved by the {@link org.roklib.urifragmentrouting.UriActionMapperTree}
 * of a {@link UriFragmentActionNavigatorWrapper}.
 * <p>
 * This view object is only created by the navigator wrapper but can be used to obtain a reference to the currently
 * executed {@link UriActionCommand}. As explained in the {@link UriFragmentActionNavigatorWrapper}, a reference to this
 * view can be obtained with a {@link ViewChangeListener}. The currently executed action command object can be obtained
 * from this view with {@link #getUriActionCommand()}.
 *
 * @see UriFragmentActionNavigatorWrapper
 */
public class ActionExecutionView implements View {
    private final UriActionCommand command;

    ActionExecutionView(final UriActionCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("action command object must not be null");
        }
        this.command = command;
    }

    @Override
    public void enter(final ViewChangeListener.ViewChangeEvent event) {
        command.run();
    }

    /**
     * Provides the {@link UriActionCommand} object which is currently executed by the {@link
     * com.vaadin.navigator.Navigator} wrapped in the {@link UriFragmentActionNavigatorWrapper}. You should not invoke
     * method {@link UriActionCommand#run()} on this object, since executing the action command object is already taken
     * care of by this view in {@link #enter(ViewChangeListener.ViewChangeEvent)}.
     *
     * @return the current {@link UriActionCommand}
     */
    public UriActionCommand getUriActionCommand() {
        return command;
    }
}


package org.vaadin.uriactions;

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

/**
 * Wrapper class around a Vaadin {@link Navigator} which adds the option to use URI fragment actions with an externally
 * defined {@link UriActionMapperTree}. The {@link Navigator} can be configured and used as usual by adding views with
 * {@link Navigator#addView(String, Class)} and the overloaded variant thereof. The reference on the wrapped {@link
 * Navigator} can be obtained with {@link #getNavigator()}. The returned navigator object can then be configured and
 * used as usual.
 * <p>
 * In addition to that, you can set a preconfigured {@link UriActionMapperTree} with {@link
 * #setUriActionMapperTree(UriActionMapperTree)}. Note that this is an optional operation. If no such object is passed
 * to this wrapper, the wrapped navigator works just like a conventional {@link Navigator}. In that case, however, a
 * plain {@link Navigator} object should be used.
 * <p>
 * When a {@link UriActionMapperTree} object has been passed to this wrapper, all URI fragments defined by this {@link
 * UriActionMapperTree} will be handled by the wrapped navigator. This means that if the navigator handles a URI
 * fragment which resolves to a {@link UriActionCommand} class as specified by the {@link UriActionMapperTree}, then
 * this command object will be executed by the navigator. More specifically, the action command will be executed in the
 * {@link View#enter(ViewChangeEvent)} method of a specific implementation of the {@link View} interface: {@link
 * ActionExecutionView}. The sole purpose of this class is to execute the resolved {@link UriActionCommand}s in its
 * {@link ActionExecutionView#enter(ViewChangeEvent)} method. <h1>The {@link UriActionMapperTree}</h1> Using a {@link
 * UriActionMapperTree} you can define a hierarchical, path-like structure of URI fragments which can be resolved to
 * command objects. Each segment of such a URI fragment can have an arbitrary number of parameters which will be
 * automatically interpreted and converted into their respective domain type. For example the action command responsible
 * for the URI fragment {@code /address/showOnMap/lon/49.508220/lat/8.523510} could show a map which is centered at the
 * coordinates given as a URI parameter. Parameter value conversion is transparently taken care of by the API so the
 * developer will directly work with a two-dimensional coordinate object instead of two Strings. <h1>Accessing the
 * current {@link UriActionCommand} object</h1> It may be necessary for an application to access the {@link
 * UriActionCommand} object for the currently interpreted URI fragment. This object is not directly accessible. It can
 * be accessed, however, with a {@link com.vaadin.navigator.ViewChangeListener} added to the wrapped {@link Navigator}.
 * In the listener's methods {@link com.vaadin.navigator.ViewChangeListener#beforeViewChange(ViewChangeEvent)} and
 * {@link com.vaadin.navigator.ViewChangeListener#afterViewChange(ViewChangeEvent)} you can check whether the new or old
 * view is of type {@link ActionExecutionView} and if so cast it into this class. You can then retrieve the current
 * {@link UriActionCommand} with {@link ActionExecutionView#getUriActionCommand()}. Refer to the following example
 * code:
 * <pre>
 *    uriFragmentActionNavigatorWrapper.getNavigator().addViewChangeListener(new ViewChangeListener() {
 *        public boolean beforeViewChange(final ViewChangeEvent event) {
 *            return true;
 *        }
 *
 *        public void afterViewChange(final ViewChangeEvent event) {
 *            if (event.getNewView() instanceof ActionExecutionView) {
 *                final ActionExecutionView view = (ActionExecutionView) event.getNewView();
 *                final UriActionCommand uriActionCommand = (UriActionCommand) view.getUriActionCommand();
 *                // do something with the command object
 *            }
 *        }
 *    });
 * </pre>
 * <h1>The Routing Context</h1> The routing context is an arbitrary user-defined object which can be passed into the URI
 * fragment interpretation process of the {@link UriActionMapperTree}. This object can be injected into the resolved
 * {@link UriActionCommand} with a method annotated with {@link org.roklib.urifragmentrouting.annotation.RoutingContext}.
 * By that, this context object can be accessed and used by the action command objects when they are executed. The
 * routing context can be used, for instance, to provide the action commands with references to service classes, such as
 * the current event bus etc. <h1>Using an alternative {@link ViewDisplay}</h1> This wrapper class provides several
 * constructors, which relate directly to the various overloaded constructors of class {@link Navigator}. Using these
 * constructors, you can add an alternative {@link ViewDisplay} to the wrapped navigator which will be used in parallel
 * with the {@link UriActionMapperTree}. If the current URI fragment could not be successfully resolved by the {@link
 * UriActionMapperTree}, the navigator tries to resolve it with the alternative {@link ViewProvider} which is
 * automatically installed when an extra {@link ViewDisplay} is set on the navigator. Using this technique, you can
 * still add views in the customary Vaadin-style where this very simple view resolution is sufficient. Using the {@link
 * UriActionMapperTree} can then be reserved for the more complex cases where the standard Vaadin mechanism is not
 * flexible enough.
 *
 * @see UriActionMapperTree
 * @see UriActionCommand
 * @see org.roklib.urifragmentrouting.annotation.RoutingContext
 */
public class UriFragmentActionNavigatorWrapper {
    /**
     * The wrapped {@link Navigator}.
     */
    private final Navigator navigator;
    private UriActionMapperTree uriActionMapperTree;
    private UriActionCommand currentActionCommandObject;
    private Object routingContext;

    /**
     * Constructs a new navigator wrapper for the given {@link UI} object. This will create the wrapped navigator with
     * the constructor {@link Navigator#Navigator(UI, ViewDisplay)}.
     *
     * @param ui the current {@link UI}
     */
    public UriFragmentActionNavigatorWrapper(final UI ui) {
        this(ui, null);
    }

    /**
     * Constructs a new navigator wrapper for the given {@link UI} object and {@link NavigationStateManager}. If the
     * given {@link NavigationStateManager} is not {@code null}, this will create the wrapped navigator with the
     * constructor {@link Navigator#Navigator(UI, NavigationStateManager, ViewDisplay)}. Otherwise, the constructor
     * {@link Navigator#Navigator(UI, ViewDisplay)} is used to create the navigator.
     *
     * @param ui                     the current {@link UI}
     * @param navigationStateManager the {@link NavigationStateManager} keeping track of the active view and enabling
     *                               bookmarking and direct navigation. May be {@code null} to use the default
     *                               navigation state manager.
     */
    public UriFragmentActionNavigatorWrapper(final UI ui, final NavigationStateManager navigationStateManager) {
        this(ui, navigationStateManager, (ViewDisplay) null);
    }

    /**
     * Constructs a new navigator wrapper for the given {@link UI} object and {@link NavigationStateManager}. If the
     * given {@link NavigationStateManager} is not {@code null}, this will create the wrapped navigator with the
     * constructor {@link Navigator#Navigator(UI, NavigationStateManager, ViewDisplay)}. Otherwise, the constructor
     * {@link Navigator#Navigator(UI, ViewDisplay)} is used to create the navigator.
     * <p>
     * This constructor allows to specify a {@link ComponentContainer} which will be wrapped by a {@link
     * ComponentContainerViewDisplay} and will be used to display any {@link View} which has not been determined by the
     * {@link UriActionMapperTree} but instead by any additional {@link ViewProvider} added to the wrapped navigator
     * with {@link Navigator#addProvider(ViewProvider)}, {@link Navigator#addView(String, Class)}, or {@link
     * Navigator#addView(String, View)}.
     *
     * @param ui                     the current {@link UI}
     * @param navigationStateManager the {@link NavigationStateManager} keeping track of the active view and enabling
     *                               bookmarking and direct navigation. May be {@code null} to use the default
     *                               navigation state manager.
     * @param container              a {@link ComponentContainer} which will be wrapped by a {@link
     *                               ComponentContainerViewDisplay}. This view display is used as an alternative {@link
     *                               ViewDisplay} (see {@link #UriFragmentActionNavigatorWrapper(UI,
     *                               NavigationStateManager, ViewDisplay)}.
     */
    public UriFragmentActionNavigatorWrapper(final UI ui, final NavigationStateManager navigationStateManager, final ComponentContainer container) {
        this(ui, navigationStateManager, new ComponentContainerViewDisplay(container));
    }

    /**
     * Constructs a new navigator wrapper for the given {@link UI} object and {@link NavigationStateManager}. If the
     * given {@link NavigationStateManager} is not {@code null}, this will create the wrapped navigator with the
     * constructor {@link Navigator#Navigator(UI, NavigationStateManager, ViewDisplay)}. Otherwise, the constructor
     * {@link Navigator#Navigator(UI, ViewDisplay)} is used to create the navigator.
     * <p>
     * This constructor allows to specify a {@link SingleComponentContainer} which will be wrapped by a {@link
     * SingleComponentContainerViewDisplay} and will be used to display any {@link View} which has not been determined
     * by the {@link UriActionMapperTree} but instead by any additional {@link ViewProvider} added to the wrapped
     * navigator with {@link Navigator#addProvider(ViewProvider)}, {@link Navigator#addView(String, Class)}, or {@link
     * Navigator#addView(String, View)}.
     *
     * @param ui                     the current {@link UI}
     * @param navigationStateManager the {@link NavigationStateManager} keeping track of the active view and enabling
     *                               bookmarking and direct navigation. May be {@code null} to use the default
     *                               navigation state manager.
     * @param container              a {@link SingleComponentContainer} which will be wrapped by a {@link
     *                               SingleComponentContainerViewDisplay}. This view display is used as an alternative
     *                               {@link ViewDisplay} (see {@link #UriFragmentActionNavigatorWrapper(UI,
     *                               NavigationStateManager, ViewDisplay)}.
     */
    public UriFragmentActionNavigatorWrapper(final UI ui, final NavigationStateManager navigationStateManager, final SingleComponentContainer container) {
        this(ui, navigationStateManager, new SingleComponentContainerViewDisplay(container));
    }

    /**
     * Constructs a new navigator wrapper for the given {@link UI} object and {@link NavigationStateManager}. If the
     * given {@link NavigationStateManager} is not {@code null}, this will create the wrapped navigator with the
     * constructor {@link Navigator#Navigator(UI, NavigationStateManager, ViewDisplay)}. Otherwise, the constructor
     * {@link Navigator#Navigator(UI, ViewDisplay)} is used to create the navigator.
     *
     * @param ui                     the current {@link UI}
     * @param navigationStateManager the {@link NavigationStateManager} keeping track of the active view and enabling
     *                               bookmarking and direct navigation. May be {@code null} to use the default
     *                               navigation state manager.
     * @param viewDisplay            alternative {@link ViewDisplay} to be used by the navigator. May be {@code null}.
     *                               If an alternative {@link ViewDisplay} is given, this will be used to display any
     *                               {@link View} which has not been determined by the {@link UriActionMapperTree} but
     *                               instead by any additional {@link ViewProvider} added to the wrapped navigator with
     *                               {@link Navigator#addProvider(ViewProvider)}, {@link Navigator#addView(String,
     *                               Class)}, or {@link Navigator#addView(String, View)}.
     */
    public UriFragmentActionNavigatorWrapper(final UI ui, final NavigationStateManager navigationStateManager, final ViewDisplay viewDisplay) {
        final UriActionViewDisplay uriActionViewDisplay = new UriActionViewDisplay(viewDisplay);

        if (navigationStateManager != null) {
            navigator = new Navigator(ui, navigationStateManager, uriActionViewDisplay);
        } else {
            navigator = new Navigator(ui, uriActionViewDisplay);
        }
        navigator.addProvider(new UriActionViewProvider());
    }

    /**
     * Provides the wrapped {@link Navigator} object.
     *
     * @return the wrapped {@link Navigator} object.
     */
    public Navigator getNavigator() {
        return navigator;
    }

    /**
     * Sets the {@link UriActionMapperTree} object for this wrapper which defines the complete hierarchical and
     * parameterizable URI fragment structure handled by the wrapped {@link Navigator}.
     *
     * @param actionMapperTree the {@link UriActionMapperTree} for this wrapper which defines the complete URI fragment
     *                         structure handled by the navigator
     */
    public void setUriActionMapperTree(final UriActionMapperTree actionMapperTree) {
        uriActionMapperTree = actionMapperTree;
    }

    /**
     * Sets the routing context object to be used for the URI fragment interpretation process. This object can be passed
     * into the {@link UriActionCommand} objects executed by the navigator.
     *
     * @param routingContext the routing context object
     * @see org.roklib.urifragmentrouting.annotation.RoutingContext
     */
    public void setRoutingContext(final Object routingContext) {
        this.routingContext = routingContext;
    }

    /**
     * Sets the current action command object to null.
     */
    void resetCurrentActionCommand() {
        currentActionCommandObject = null;
    }

    /**
     * {@link ViewDisplay} which delegates the task to display the current {@link View} to a wrapped {@link ViewDisplay}
     * if the {@link View} to be shown is <em>not</em> of type {@link ActionExecutionView}. The wrapped {@link
     * ViewDisplay} can be provided by one of the constructors which accepts either a {@link ComponentContainer},
     * a {@link SingleComponentContainer}, or a {@link ViewDisplay}.
     */
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

    /**
     * {@link ViewProvider} which resolves the current URI fragment against the current {@link UriActionMapperTree} and
     * creates a {@link ActionExecutionView} object with the corresponding {@link UriActionCommand} object if the
     * fragment could successfully be resolved.
     */
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
            return new ActionExecutionView(UriFragmentActionNavigatorWrapper.this, currentActionCommandObject);
        }
    }
}

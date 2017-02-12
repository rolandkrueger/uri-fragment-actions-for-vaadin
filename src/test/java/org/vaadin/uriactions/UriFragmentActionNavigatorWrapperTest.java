package org.vaadin.uriactions;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.navigator.ViewDisplay;
import org.junit.Before;
import org.junit.Test;
import org.roklib.urifragmentrouting.UriActionCommand;
import org.roklib.urifragmentrouting.UriActionMapperTree;
import org.roklib.urifragmentrouting.annotation.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.uriactions.testhelpers.TestNavigationStateHandler;
import org.vaadin.uriactions.testhelpers.TestUI;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class UriFragmentActionNavigatorWrapperTest {
    private final static Logger LOG = LoggerFactory.getLogger(UriFragmentActionNavigatorWrapperTest.class);

    private UriFragmentActionNavigatorWrapper uriFragmentActionNavigatorWrapper;
    private TestNavigationStateHandler navigationStateHandler;
    private UriActionMapperTree uriActionMapperTree;

    @Before
    public void setUp() {
        navigationStateHandler = new TestNavigationStateHandler();
        uriFragmentActionNavigatorWrapper = new UriFragmentActionNavigatorWrapper(new TestUI(), navigationStateHandler, null);
        uriActionMapperTree = UriActionMapperTree.create().buildMapperTree().build();
    }

    @Test
    public void testUriFragmentActionNavigator() {
        uriActionMapperTree = UriActionMapperTree.create().buildMapperTree()
                .map("test")
                .onActionFactory(TestActionCommand::new)
                .finishMapper().build();

        uriFragmentActionNavigatorWrapper.setUriActionMapperTree(uriActionMapperTree);
        uriFragmentActionNavigatorWrapper.setRoutingContext(new MyRoutingContext("contextData"));
        uriFragmentActionNavigatorWrapper.getNavigator().addViewChangeListener(new ViewChangeListener() {
            @Override
            public boolean beforeViewChange(final ViewChangeEvent event) {
                LOG.info("Before view change. New view: {}", event.getNewView());
                return true;
            }

            @Override
            public void afterViewChange(final ViewChangeEvent event) {
                LOG.info("After view change. New view: {}", event.getNewView());
                final ActionExecutionView view = (ActionExecutionView) event.getNewView();
                final TestActionCommand uriActionCommand = (TestActionCommand) view.getUriActionCommand();
                assertTrue("Action command was not executed.", uriActionCommand.isExecuted());
                assertThat(uriActionCommand.getRoutingContext().getData(), equalTo("contextData"));
            }
        });

        navigationStateHandler.setState("/test");
        uriFragmentActionNavigatorWrapper.getNavigator().navigateTo("/test");
    }

    @Test
    public void testProvideOwnViewDisplay() {
        final TestViewDisplay viewDisplay = new TestViewDisplay();
        uriFragmentActionNavigatorWrapper = new UriFragmentActionNavigatorWrapper(new TestUI(), navigationStateHandler, viewDisplay);

        uriActionMapperTree = UriActionMapperTree.create().buildMapperTree()
                .map("test")
                .onActionFactory(TestActionCommand::new)
                .finishMapper().build();

        uriFragmentActionNavigatorWrapper.setUriActionMapperTree(uriActionMapperTree);
        uriFragmentActionNavigatorWrapper.getNavigator().addView("separate_view", (View) event -> {
        });
        uriFragmentActionNavigatorWrapper.getNavigator().addViewChangeListener(new ViewChangeListener() {
            @Override
            public boolean beforeViewChange(final ViewChangeEvent event) {
                return true;
            }

            @Override
            public void afterViewChange(final ViewChangeEvent event) {
                assertFalse(event.getNewView() instanceof ActionExecutionView);
            }
        });

        uriFragmentActionNavigatorWrapper.getNavigator().navigateTo("separate_view");
        assertTrue("Separately provided view display was not activated.", viewDisplay.viewShown);
    }

    private static class TestViewDisplay implements ViewDisplay {
        public boolean viewShown = false;

        @Override
        public void showView(final View view) {
            viewShown = true;
        }
    }

    private static class MyRoutingContext {
        private final String data;

        public MyRoutingContext(final String data) {
            this.data = data;
        }

        public String getData() {
            return data;
        }
    }

    public static class TestActionCommand implements UriActionCommand {
        private boolean executed = false;
        private MyRoutingContext routingContext;

        @RoutingContext
        public void setRoutingContext(final MyRoutingContext routingContext) {
            this.routingContext = routingContext;
        }

        public MyRoutingContext getRoutingContext() {
            return routingContext;
        }

        @Override
        public void run() {
            executed = true;
        }

        public boolean isExecuted() {
            return executed;
        }
    }
}

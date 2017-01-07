package org.vaadin.uriactions;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewDisplay;
import org.junit.Before;
import org.junit.Test;
import org.roklib.urifragmentrouting.UriActionCommand;
import org.roklib.urifragmentrouting.UriActionMapperTree;
import org.vaadin.uriactions.testhelpers.TestNavigationStateHandler;
import org.vaadin.uriactions.testhelpers.TestUI;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class UriFragmentActionNavigatorTest {
    private UriFragmentActionNavigator testObj;
    private TestNavigationStateHandler navigationStateHandler;
    private UriActionMapperTree uriActionMapperTree;

    @Before
    public void setUp() {
        navigationStateHandler = new TestNavigationStateHandler();
        testObj = new UriFragmentActionNavigator(new TestUI(), navigationStateHandler);
        uriActionMapperTree = UriActionMapperTree.create().buildMapperTree().build();
    }

    @Test
    public void testAddHandler() {
        final TestActionCommand cmd = new TestActionCommand();

        uriActionMapperTree = UriActionMapperTree.create().buildMapperTree()
                .map("test")
                .onAction(TestActionCommand.class)
                .finishMapper().build();

        testObj.setUriActionMapperTree(uriActionMapperTree);
        navigationStateHandler.setState("/test");
        testObj.getNavigator().navigateTo("/test");
        assertTrue("Action command was not executed.", cmd.isExecuted());
    }

    @Test
    public void testProvideOwnViewDisplay() {
        final TestViewDisplay viewDisplay = new TestViewDisplay();
        testObj = new UriFragmentActionNavigator(new TestUI(), navigationStateHandler, viewDisplay);
        final TestActionCommand cmd = new TestActionCommand();

        uriActionMapperTree = UriActionMapperTree.create().buildMapperTree()
                .map("test")
                .onAction(TestActionCommand.class)
                .finishMapper().build();

        testObj.setUriActionMapperTree(uriActionMapperTree);
        testObj.getNavigator().addView("separate_view", (View) event -> {
        });

        testObj.getNavigator().navigateTo("separate_view");
        assertTrue("Separately provided view display was not activated.", viewDisplay.viewShown);
        assertFalse("Action command was unexpectedly executed.", cmd.isExecuted());
    }

    private static class TestViewDisplay implements ViewDisplay {
        public boolean viewShown = false;

        @Override
        public void showView(final View view) {
            viewShown = true;
        }
    }

    private static class TestActionCommand implements UriActionCommand {
        private boolean executed = false;

        @Override
        public void run() {
            executed = true;
        }

        public boolean isExecuted() {
            return executed;
        }
    }
}

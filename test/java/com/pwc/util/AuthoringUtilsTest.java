package com.pwc.util;

import com.day.cq.wcm.api.AuthoringUIMode;
import com.pwc.util.AuthoringUtils;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import org.apache.sling.api.SlingHttpServletRequest;
import org.junit.Before;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;


import javax.script.Bindings;
import javax.servlet.ServletRequest;
import java.io.IOException;

import static com.day.cq.wcm.api.AuthoringUIMode.REQUEST_ATTRIBUTE_NAME;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class AuthoringUtilsTest {


    @Mock
   private ServletRequest request;

    private Bindings bindings;


    @Before
    public void setUp() {
        bindings = mock(Bindings.class);
        when(bindings.get("request")).thenReturn(request);
    }
    @Test
    public void testIsTouch() {
         //   when(request.getAttribute(AuthoringUIMode.REQUEST_ATTRIBUTE_NAME)).thenReturn(AuthoringUIMode.TOUCH);
        request.setAttribute(REQUEST_ATTRIBUTE_NAME, AuthoringUIMode.TOUCH);

        assertTrue(AuthoringUtils.isTouch(request));
        verify(request, times(1)).getAttribute(AuthoringUIMode.class.getName());
        verifyNoMoreInteractions(request);


    }
}

package org.qualitydxb.infrastructure;

import org.qualitydxb.common.Enums.LogTag;
import org.qualitydxb.common.Enums.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.io.StringWriter;

public class LoggerService {
    private static final Logger logger = LoggerFactory.getLogger(LoggerService.class);

    public static void log(Exception e, Project project, LogTag tag) {
        StackTraceElement element = e.getStackTrace()[0];

        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        String stackTrace = sw.toString();

        logger.error("Project: {}, Tag: {}, Line: {}, Exception: {}\nStackTrace:\n{}",
                project,
                tag,
                element.getLineNumber(),
                e.getMessage(),
                stackTrace);
    }
}

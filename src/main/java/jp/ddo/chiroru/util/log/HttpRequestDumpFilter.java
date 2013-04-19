package jp.ddo.chiroru.util.log;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.Time;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.StreamHandler;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * javax.servlet.Filterを実装してHTTPリクエストの内容を解析してログを出力する
 * Filterクラス。
 */
public class HttpRequestDumpFilter implements Filter {

    private static Logger logger = 
        Logger.getLogger(HttpRequestDumpFilter.class.getName());
    private static final String LINE_SEPA = 
        System.getProperty("line.separator");
    private static final String NEXT_PAGE = "LogFilter.NEXT_PAGE";
    
    /**
     * このログフィルタを初期化します。
     * @param mapping
     */
    public void init(FilterConfig mapping) {
        String str = mapping.getInitParameter("logging.Level");
        System.out.println("ログレベルを"+str+"に設定します。");
        Level level = null;
        try {
            level = Level.parse(str);
        } catch (Exception e) {
            e.printStackTrace();
            level = Level.INFO;
        }
        LogManager.getLogManager().reset();
        Handler handler = new CustomConsoleHandler();
        handler.setFormatter(new CustomFormatter());
        handler.setLevel(level);
        logger.setLevel(level);
        logger.getParent().addHandler(handler);
    }

    /**
     * ログを出力するフィルタです。
     * @param request 処理しているHTTPリクエスト
     * @param response 生成中のHTTPレスポンス
     * @param chain
     */
    public void doFilter(ServletRequest _request, ServletResponse _response,
            FilterChain chain) throws IOException, ServletException {

        // -----------------------------------------------------------『 前処理 』
        HttpServletRequest request = (HttpServletRequest) _request;
        HttpServletResponse response = (HttpServletResponse) _response;
        if (logger.isLoggable(Level.CONFIG)) {
            logger.config(
                "============ Request Start !! " 
                +"Thread ID:" 
                + Thread.currentThread().hashCode()
                + " ========================================================");
        }
        // メモリ使用量
        String actionMemory = null;
        if (logger.isLoggable(Level.CONFIG)) {
            actionMemory = getMemoryInfo(" "
                    + new Time(System.currentTimeMillis()) + " リクエスト[前]");
        }
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("Cookie情報" + getCookieInfo(request));
        }
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("HTTPヘッダ情報" + getHeadersInfo(request));
        }
        if (logger.isLoggable(Level.FINE)) {
            logger
                    .fine("HTTPその他の情報" + getRequestOtherInfo(request));
        }
        if (logger.isLoggable(Level.CONFIG)) {
            String reqlog = getRequestParametersInfo(request);
            logger.config("HTTPリクエストパラメータ" + reqlog);
        }
        if (logger.isLoggable(Level.CONFIG)) {
            logger.config("requestスコープのオブジェクト"
                    + getRequestAttributeInfo(request));
        }
        if (logger.isLoggable(Level.CONFIG)) {
            String sessionlog = getSessionInfo(request,true);
            logger.config("sessionスコープのオブジェクト(リクエスト処理前)" 
                    + sessionlog);
        }
        
        // 次のフィルタを呼び出し
        chain.doFilter(request, response);

        // ----------------------------------------------------------『 後処理 』
        if (logger.isLoggable(Level.CONFIG)) {
            String sessionlog = getSessionInfo(request,false);
            logger.config("sessionスコープのオブジェクト(リクエスト処理後)" 
                    + sessionlog);
        }
        // メモリ使用量
        if (logger.isLoggable(Level.CONFIG)) {
            actionMemory = " リクエスト前後のメモリ使用量"+LINE_SEPA
                    + actionMemory + LINE_SEPA
                    + getMemoryInfo(" " + new Time(System.currentTimeMillis())
                            + " リクエスト[後]");
            logger.config(actionMemory+LINE_SEPA);

        }
        // 画面遷移情報
        if (logger.isLoggable(Level.INFO)) {
            String nextPage = (String) request.getAttribute(NEXT_PAGE);
            if (nextPage == null || nextPage.length() == 0) {
                nextPage = request.getRequestURI();
            }
            logger.info("NEXT_PAGE=[" + nextPage + "], "
                    + "IP_ADDRESS=[" + request.getRemoteAddr() + "], " 
                    + "SESSION_ID=[" + request.getSession().getId() + "], " 
                    + "USER-AGENT=[" + request.getHeader("user-agent") + "]");
        }
        if (logger.isLoggable(Level.CONFIG)) {
            logger.config(
                "============ Request End  !! " 
                +"Thread ID:"+ Thread.currentThread().hashCode()
                + " ========================================================="
                +LINE_SEPA+LINE_SEPA);
        }
    }

    /**
     * 
     */
    public void destroy() {
    }

    // －－－－－－－－－－　以下プライベートメソッド　－－－－－－－－－－－
    private static String getMemoryInfo(String message) {
        DecimalFormat dFromat = new DecimalFormat("#,###KB");
        long free = Runtime.getRuntime().freeMemory() / 1024;
        long total = Runtime.getRuntime().totalMemory() / 1024;
        long max = Runtime.getRuntime().maxMemory() / 1024;
        long used = total - free;
        String msg = message + "  : " + "合計=" + dFromat.format(total) + ", "
                + "使用量=" + dFromat.format(used) + " (" + (used * 100 / total)
                + "%), 使用可能最大=" + dFromat.format(max);
        return msg;
    }
    /**
     * リクエストヘッダをすべてログに出力する。
     */
    private static String getHeadersInfo(HttpServletRequest request) {
        StringBuffer buff = new StringBuffer(LINE_SEPA);
        Enumeration headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = (String) headerNames.nextElement();
            buff.append("    ");
            buff.append(headerName);
            buff.append("=");
            buff.append(request.getHeader(headerName));
            buff.append(LINE_SEPA);
        }
        return buff.toString();
    }
    private static String getCookieInfo(HttpServletRequest request) {
        StringBuffer buff = new StringBuffer();
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return "";
        }
        for (int i = 0; i < cookies.length; i++) {
            buff.append("\n  --- Cookie[" + i + "] ---\n");
            buff.append("    ");
            buff.append(cookies[i].getName());
            buff.append("=");
            buff.append(cookies[i].getValue());
            buff.append(LINE_SEPA);
            buff.append("    ");
            buff.append("getVersion()");
            buff.append("=");
            buff.append(cookies[i].getVersion());
            buff.append(LINE_SEPA);
            buff.append("    ");
            buff.append("getComment()");
            buff.append("=");
            buff.append(cookies[i].getComment());
            buff.append(LINE_SEPA);
            buff.append("    ");
            buff.append("getDomain()");
            buff.append("=");
            buff.append(cookies[i].getDomain());
            buff.append(LINE_SEPA);
            buff.append("    ");
            buff.append("getMaxAge()");
            buff.append("=");
            buff.append(cookies[i].getMaxAge());
            buff.append(LINE_SEPA);
            buff.append("    ");
            buff.append("getPath()");
            buff.append("=");
            buff.append(cookies[i].getPath());
            buff.append(LINE_SEPA);
            buff.append("    ");
            buff.append("getSecure()");
            buff.append("=");
            buff.append(cookies[i].getSecure());
            buff.append(LINE_SEPA);
        }
        return buff.toString();
    }
    private static String getRequestParametersInfo(HttpServletRequest request) {
        StringBuffer buff = new StringBuffer(LINE_SEPA);
        Map map = convertRequest(request);
        TreeMap trr = new TreeMap(map);
        Iterator itr = trr.keySet().iterator();
        while (itr.hasNext()) {
            String key = (String) itr.next();
            buff.append("    ");
            buff.append(key);
            buff.append("=");
            Object value = map.get(key);
            String[] values = (String[]) value;
            if (values.length == 1) {
                buff.append(values[0]);
            } else {
                // String 配列は変換する
                String strValue = stratum(values);
                buff.append(strValue);
            }
            buff.append(LINE_SEPA);
        }
        return buff.toString();
    }
    private static String getRequestAttributeInfo(HttpServletRequest request) {
        StringBuffer buff = new StringBuffer(LINE_SEPA);
        Enumeration e = request.getAttributeNames();
        while (e.hasMoreElements()) {
            String name = (String) e.nextElement();
            buff.append("    name=" + name + ", attributeClass= "
                    + request.getAttribute(name).getClass().getName()
                    + ", toString() = " + request.getAttribute(name)
                    + LINE_SEPA);
        }
        return buff.toString();
    }
    private static String getRequestOtherInfo(HttpServletRequest request) {
        StringBuffer buff = new StringBuffer();
        buff.append(LINE_SEPA);
        buff.append("    getCharacterEncoding()=");
        buff.append(request.getCharacterEncoding());
        buff.append(LINE_SEPA);
        buff.append("    getContentLength()=");
        buff.append(request.getContentLength());
        buff.append(LINE_SEPA);
        buff.append("    getContentType()=");
        buff.append(request.getContentType());
        buff.append(LINE_SEPA);
        buff.append("    getLocale()=");
        buff.append(request.getLocale());
        buff.append(LINE_SEPA);
        buff.append("    getProtocol()=");
        buff.append(request.getProtocol());
        buff.append(LINE_SEPA);
        buff.append("    getRemoteAddr()=");
        buff.append(request.getRemoteAddr());
        buff.append(LINE_SEPA);
        buff.append("    getRemoteHost()=");
        buff.append(request.getRemoteHost());
        buff.append(LINE_SEPA);
        buff.append("    getScheme()=");
        buff.append(request.getScheme());
        buff.append(LINE_SEPA);
        buff.append("    getServerName()=");
        buff.append(request.getServerName());
        buff.append(LINE_SEPA);
        buff.append("    getServerPort()=");
        buff.append(request.getServerPort());
        buff.append(LINE_SEPA);
        buff.append("    isSecure()=");
        buff.append(request.isSecure());
        buff.append(LINE_SEPA);
        buff.append("    getAuthType()=");
        buff.append(request.getAuthType());
        buff.append(LINE_SEPA);
        buff.append("    getContextPath()=");
        buff.append(request.getContextPath());
        buff.append(LINE_SEPA);
        buff.append("    getMethod()=");
        buff.append(request.getMethod());
        buff.append(LINE_SEPA);
        buff.append("    getPathInfo()=");
        buff.append(request.getPathInfo());
        buff.append(LINE_SEPA);
        buff.append("    getPathTranslated()=");
        buff.append(request.getPathTranslated());
        buff.append(LINE_SEPA);
        buff.append("    getQueryString()=");
        buff.append(request.getQueryString());
        buff.append(LINE_SEPA);
        buff.append("    getRemoteUser()=");
        buff.append(request.getRemoteUser());
        buff.append(LINE_SEPA);
        buff.append("    getRequestedSessionId()=");
        buff.append(request.getRequestedSessionId());
        buff.append(LINE_SEPA);
        buff.append("    getRequestURI()=");
        buff.append(request.getRequestURI());
        buff.append(LINE_SEPA);
        buff.append("    getServletPath()=");
        buff.append(request.getServletPath());
        buff.append(LINE_SEPA);
        buff.append("    getUserPrincipal()=");
        buff.append(request.getUserPrincipal());
        buff.append(LINE_SEPA);
        buff.append("    isRequestedSessionIdFromCookie()=");
        buff.append(request.isRequestedSessionIdFromCookie());
        buff.append(LINE_SEPA);
        buff.append("    isRequestedSessionIdFromURL()=");
        buff.append(request.isRequestedSessionIdFromURL());
        buff.append(LINE_SEPA);
        buff.append("    isRequestedSessionIdValid()=");
        buff.append(request.isRequestedSessionIdValid());
        buff.append(LINE_SEPA);
        return buff.toString();
    }
    
    private static String getSessionInfo(HttpServletRequest request,boolean before) {
        HttpSession session = request.getSession();
        StringBuffer buff = new StringBuffer();
        buff.append(LINE_SEPA);
        if (before) {
            buff.append("    session.isNew() = " + session.isNew());
            buff.append(LINE_SEPA);
            buff.append("    session.getId() = " + session.getId());
            buff.append(LINE_SEPA);
        }
        Enumeration e = session.getAttributeNames();
        while (e.hasMoreElements()) {
            String sessionName = (String) e.nextElement();
            String sessionClassName = session.getAttribute(sessionName)
                    .getClass().getName();
            buff.append("    name =" + sessionName + ", value ="
                    + session.getAttribute(sessionName) + ", attributeClass = "
                    + sessionClassName+LINE_SEPA);
        }
        return buff.toString();
    }
    
    private static Hashtable convertRequest(HttpServletRequest request) {
        Hashtable tempHash = new Hashtable();
        Enumeration e = request.getParameterNames();
        while (e.hasMoreElements()) {
            String key = (String) e.nextElement();
            String[] values = request.getParameterValues(key);
            String[] parameterValues = new String[values.length];
            for (int i = 0; i < values.length; i++) {
                parameterValues[i] = convUnicode(values[i]);
            }
            tempHash.put(key, parameterValues);
        }
        return tempHash;

    }
    private static String stratum(Object value) {
        if (value == null) {
            return "null";
        } else if (value instanceof String[]) {
            return convString((String[]) value);
        } else {
            return value.toString();
        }
    }

    /**
     * 文字列配列[strArray]の内容を、下記のようなStringで返します。
     *  "[temp1,temp2,temp3]"
     * @param strArray 評価対象のString配列
     * @return 変換後の文字列
     */
    private static String convString(String[] strArray) {
        if (strArray == null)
            return null;
        StringBuffer buff = new StringBuffer("[");
        for (int i = 0; i < strArray.length; i++) {
            buff.append(strArray[i] + ", ");
        }
        buff.delete(buff.length() - 2, buff.length());
        buff.append("]");
        return buff.toString();
    }

    /**
     * [str]をユニコードへ変換します。
     * @param str
     * @return
     */
    private static String convUnicode(String str) {
        if (str == null)
            return null;
        try {
            return new String(str.getBytes("8859_1"), "JISAutoDetect");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
    // -----------------------------------------------------------------------
    static class CustomFormatter extends Formatter {
        static final String pattern = "yyyy/MM/dd HH:mm:ss";
        public synchronized String format(LogRecord record) {
            StringBuffer buf = new StringBuffer();
            // 日時を設定
            Date date = new Date();
            date.setTime(record.getMillis());
            SimpleDateFormat formatter = new SimpleDateFormat(pattern);
            buf.append(formatter.format(date));
            buf.append(":");
            // レベルを設定
            buf.append("[" + record.getLevel().getName() + "]");
            buf.append(":");
            buf.append(record.getMessage());
            buf.append(LINE_SEPA);
            return buf.toString();
        }
    }
    static class CustomConsoleHandler extends StreamHandler {
        public CustomConsoleHandler() {
            super();
            setOutputStream(System.out);
        }
        /**
         * LogRecord を発行します。
         * 初期状態では、ロギングの要求は Logger オブジェクトに
         * 対して行われ、このオブジェクトは LogRecord を初期化して
         * ここに転送しました。
         * 
         * @param   record ログイベントの説明。null レコードは単に無視される
         *                 だけで、通知は行われない
         */
        public void publish(LogRecord record) {
            super.publish(record);  
            flush();
        }
        /**
         * StreamHandler.close をオーバーライドしてフラッシュしますが、
         * 出力ストリームは閉じません。つまり、System.err は閉じません。
         */
        public void close() {
            flush();
        }
    }
}

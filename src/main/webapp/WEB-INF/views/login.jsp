<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="titleKey" value="login.title"/><c:set var="mainClass" value="narrow"/>
<%@ include file="_header.jspf" %>

<section class="card form-card" aria-labelledby="login-title">
  <h1 id="login-title"><fmt:message key="login.title"/></h1>
  
  <c:if test="${param.registered == '1'}">
    <p class="ok" role="status"><fmt:message key="login.registered"/></p>
  </c:if>
  
  <c:if test="${not empty error}">
    <p class="error" role="alert"><fmt:message key="${error}"/></p>
  </c:if>
  
  <form method="post" action="${ctx}/login">
    <input type="hidden" name="csrf" value="<c:out value='${sessionScope.csrfToken}'/>">
    
    <label class="field">
      <fmt:message key="login.identifier"/>
      <input type="text" name="identifier" value="<c:out value='${identifier}'/>" maxlength="255" autocomplete="username" required autofocus>
    </label>
    
    <label class="field">
      <fmt:message key="login.password"/>
      <input type="password" name="password" autocomplete="current-password" required>
    </label>
    
    <button type="submit" class="btn btn-primary block"><fmt:message key="login.submit"/></button>
  </form>
  
  <p class="form-footer">
    <fmt:message key="login.noaccount"/> <a href="${ctx}/register"><fmt:message key="register.title"/></a>
  </p>
</section>

<%@ include file="_footer.jspf" %>

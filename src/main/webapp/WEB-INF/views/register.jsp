<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="titleKey" value="register.title"/><c:set var="mainClass" value="narrow"/>
<%@ include file="_header.jspf" %>

<section class="card form-card" aria-labelledby="register-title">
  <h1 id="register-title"><fmt:message key="register.title"/></h1>
  
  <c:forEach var="e" items="${errors}">
    <p class="error" role="alert"><fmt:message key="${e}"/></p>
  </c:forEach>
  
  <form method="post" action="${ctx}/register">
    <input type="hidden" name="csrf" value="<c:out value='${sessionScope.csrfToken}'/>">
    
    <label class="field">
      <fmt:message key="register.username"/>
      <input type="text" name="username" value="<c:out value='${username}'/>" maxlength="30" autocomplete="username" required>
    </label>
    
    <label class="field">
      <fmt:message key="register.email"/>
      <input type="email" name="email" value="<c:out value='${email}'/>" maxlength="255" autocomplete="email" required>
    </label>
    
    <label class="field">
      <fmt:message key="register.password"/>
      <input type="password" name="password" minlength="8" autocomplete="new-password" required>
    </label>
    
    <label class="field">
      <fmt:message key="register.confirm"/>
      <input type="password" name="confirm" minlength="8" autocomplete="new-password" required>
    </label>
    
    <button type="submit" class="btn btn-primary block"><fmt:message key="register.submit"/></button>
  </form>
  
  <p class="form-footer">
    <fmt:message key="register.haveaccount"/> <a href="${ctx}/login"><fmt:message key="login.title"/></a>
  </p>
</section>

<%@ include file="_footer.jspf" %>

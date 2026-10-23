<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="titleKey" value="library.title"/><c:set var="mainClass" value="wide"/>
<%@ include file="_header.jspf" %>
<section id="library" class="library" aria-labelledby="library-title">
  <div class="library-shell">
    <aside class="library-rail" aria-label="<fmt:message key='nav.library'/>">
      <span class="library-rail__number" aria-hidden="true">01</span>
      <span class="library-rail__label"><fmt:message key="library.title"/></span>
      <span class="library-rail__rule" aria-hidden="true"></span>
      <span class="library-rail__caption"><fmt:message key="app.kicker"/></span>
    </aside>
    <div class="library-content">
      <div class="page-heading"><div><span class="eyebrow"><fmt:message key="app.title"/></span><h1 id="library-title"><fmt:message key="library.title"/></h1></div><div class="library-toolbar"><a href="${ctx}/import" class="btn primary"><svg aria-hidden="true" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 3v12"/><path d="m7 10 5 5 5-5"/><path d="M5 21h14"/></svg><fmt:message key="library.import"/></a></div></div>
  <c:choose><c:when test="${empty books}"><div class="empty-state"><div class="empty-state__inner"><span class="empty-state__icon" aria-hidden="true"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20"/><path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2Z"/></svg></span><p><fmt:message key="library.empty"/></p><a href="${ctx}/import" class="btn"><fmt:message key="library.import"/></a></div></div></c:when><c:otherwise><div class="grid"><c:forEach var="b" items="${books}"><article class="book"><a class="cover" href="${ctx}/read?id=${b.id}" aria-label="<c:out value='${b.title}'/>"><c:choose><c:when test="${b.hasCover}"><img src="${ctx}/books/cover?id=${b.id}" alt="" loading="lazy"></c:when><c:otherwise><span class="placeholder" aria-hidden="true">PDF</span></c:otherwise></c:choose><c:if test="${not empty b.lastReadAt}"><span class="bar"><span style="width:${b.progressPercent}%"></span></span></c:if></a><div class="info"><a class="title" href="${ctx}/read?id=${b.id}" title="<c:out value='${b.title}'/>"><c:out value="${b.title}"/></a><div class="sub"><span class="pages"><fmt:message key="reader.page"/> ${b.lastPage} / ${b.pageCount}</span><fmt:message key="book.delete.confirm" var="confirmMsg"><fmt:param value="${b.title}"/></fmt:message><form method="post" action="${ctx}/books/delete" class="delete-form" data-confirm="<c:out value='${confirmMsg}'/>"><input type="hidden" name="csrf" value="<c:out value='${sessionScope.csrfToken}'/>"><input type="hidden" name="id" value="${b.id}"><button type="submit" class="link danger"><fmt:message key="book.delete"/></button></form></div></div></article></c:forEach></div></c:otherwise></c:choose>
    </div>
  </div>
</section><script src="${ctx}/static/js/library.js" defer></script><%@ include file="_footer.jspf" %>

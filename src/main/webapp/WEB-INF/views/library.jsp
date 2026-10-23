<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="titleKey" value="library.title"/><c:set var="mainClass" value="wide"/>
<%@ include file="_header.jspf" %>

<section id="library" class="library-view" aria-labelledby="library-title">
  <!-- Page Header -->
  <div class="page-heading">
    <div class="page-heading__content">
      <h1 id="library-title"><fmt:message key="library.title"/></h1>
    </div>
    <div class="page-heading__actions">
      <a href="${ctx}/import" class="btn btn-primary">
        <svg aria-hidden="true" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <line x1="12" y1="5" x2="12" y2="19"></line>
          <line x1="5" y1="12" x2="19" y2="12"></line>
        </svg>
        <span><fmt:message key="library.import"/></span>
      </a>
    </div>
  </div>

  <!-- Search & Filter Controls -->
  <form class="library-controls" method="get" action="${ctx}/home" role="search">
    <div class="search-box">
      <svg class="search-icon" aria-hidden="true" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
        <circle cx="11" cy="11" r="8"></circle>
        <line x1="21" y1="21" x2="16.65" y2="16.65"></line>
      </svg>
      <input type="search" name="q" value="<c:out value='${query}'/>" placeholder="<fmt:message key='library.search'/>..." aria-label="<fmt:message key='library.search'/>">
      <c:if test="${not empty query}">
        <a href="${ctx}/home" class="search-clear-btn" title="Effacer la recherche" aria-label="Effacer">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <line x1="18" y1="6" x2="6" y2="18"></line>
            <line x1="6" y1="6" x2="18" y2="18"></line>
          </svg>
        </a>
      </c:if>
    </div>

    <div class="sort-box">
      <label class="sr" for="sortSelect"><fmt:message key="library.sort"/></label>
      <svg class="sort-icon" aria-hidden="true" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
        <line x1="4" y1="6" x2="20" y2="6"></line>
        <line x1="4" y1="12" x2="14" y2="12"></line>
        <line x1="4" y1="18" x2="9" y2="18"></line>
      </svg>
      <select id="sortSelect" name="sort" onchange="this.form.submit()">
        <option value="recent" ${sort == 'recent' ? 'selected' : ''}><fmt:message key="library.sort.recent"/></option>
        <option value="added" ${sort == 'added' ? 'selected' : ''}><fmt:message key="library.sort.added"/></option>
        <option value="title" ${sort == 'title' ? 'selected' : ''}><fmt:message key="library.sort.title"/></option>
      </select>
      <svg class="chevron-icon" aria-hidden="true" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
        <polyline points="6 9 12 15 18 9"></polyline>
      </svg>
    </div>

    <button type="submit" class="btn btn-search">
      <svg aria-hidden="true" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
        <circle cx="11" cy="11" r="8"></circle>
        <line x1="21" y1="21" x2="16.65" y2="16.65"></line>
      </svg>
      <span><fmt:message key="library.search.btn"/></span>
    </button>
  </form>

  <!-- Continue Reading Hero -->
  <c:if test="${not empty continueBook and empty query}">
    <section class="continue-hero" aria-labelledby="continue-title">
      <div class="continue-hero__inner">
        <a href="${ctx}/read?id=${continueBook.id}" class="continue-hero__cover" tabindex="-1" aria-hidden="true">
          <c:choose>
            <c:when test="${continueBook.hasCover}">
              <img src="${ctx}/books/cover?id=${continueBook.id}" alt="">
            </c:when>
            <c:otherwise>
              <div class="pdf-fallback-stamp"><span>PDF</span></div>
            </c:otherwise>
          </c:choose>
          <div class="cover-spine-effect"></div>
        </a>
        <div class="continue-hero__body">
          <div class="continue-hero__header">
            <span class="continue-tag" id="continue-title">
              <svg aria-hidden="true" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="m19 21-7-4-7 4V5a2 2 0 0 1 2-2h10a2 2 0 0 1 2 2v16z"></path>
              </svg>
              <fmt:message key="library.continue"/>
            </span>
            <a href="${ctx}/read?id=${continueBook.id}" class="continue-hero__title" title="<c:out value='${continueBook.title}'/>">
              <c:out value="${continueBook.title}"/>
            </a>
          </div>
          <div class="continue-hero__progress-row">
            <div class="progress-track" role="progressbar" aria-valuenow="${continueBook.progressPercent}" aria-valuemin="0" aria-valuemax="100">
              <div class="progress-bar" style="width: ${continueBook.progressPercent}%;"></div>
            </div>
            <div class="continue-hero__stats">
              <span class="pages-badge"><fmt:message key="reader.page"/> ${continueBook.lastPage} / ${continueBook.pageCount}</span>
              <span class="percent-badge">${continueBook.progressPercent}%</span>
            </div>
          </div>
        </div>
        <div class="continue-hero__action">
          <a href="${ctx}/read?id=${continueBook.id}" class="btn btn-resume">
            <span><fmt:message key="library.resume"/></span>
            <svg aria-hidden="true" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <line x1="5" y1="12" x2="19" y2="12"></line>
              <polyline points="12 5 19 12 12 19"></polyline>
            </svg>
          </a>
        </div>
      </div>
    </section>
  </c:if>

  <!-- Books Presentation -->
  <c:choose>
    <c:when test="${empty books}">
      <div class="empty-state">
        <div class="empty-state__inner">
          <div class="empty-state__icon" aria-hidden="true">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
              <path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20"/>
              <path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2Z"/>
            </svg>
          </div>
          <h2>
            <c:choose>
              <c:when test="${not empty query}">
                <fmt:message key="library.noResults"/>
              </c:when>
              <c:otherwise>
                <fmt:message key="library.empty"/>
              </c:otherwise>
            </c:choose>
          </h2>
          <c:if test="${empty query}">
            <a href="${ctx}/import" class="btn btn-primary">
              <svg aria-hidden="true" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <line x1="12" y1="5" x2="12" y2="19"></line>
                <line x1="5" y1="12" x2="19" y2="12"></line>
              </svg>
              <span><fmt:message key="library.import"/></span>
            </a>
          </c:if>
        </div>
      </div>
    </c:when>
    <c:otherwise>
      <div class="books-grid">
        <c:forEach var="b" items="${books}">
          <article class="book-card">
            <div class="book-card__cover-wrap">
              <a class="book-card__cover" href="${ctx}/read?id=${b.id}" aria-label="<c:out value='${b.title}'/>">
                <c:choose>
                  <c:when test="${b.hasCover}">
                    <img src="${ctx}/books/cover?id=${b.id}" alt="" loading="lazy">
                  </c:when>
                  <c:otherwise>
                    <div class="pdf-fallback-stamp"><span>PDF</span></div>
                  </c:otherwise>
                </c:choose>
                <div class="cover-spine-effect" aria-hidden="true"></div>
                <c:if test="${not empty b.lastReadAt}">
                  <div class="book-progress-line" aria-hidden="true">
                    <span style="width: ${b.progressPercent}%;"></span>
                  </div>
                </c:if>
              </a>

              <fmt:message key="book.delete.confirm" var="confirmMsg"><fmt:param value="${b.title}"/></fmt:message>
              <form method="post" action="${ctx}/books/delete" class="delete-form book-card__delete-form" data-confirm="<c:out value='${confirmMsg}'/>">
                <input type="hidden" name="csrf" value="<c:out value='${sessionScope.csrfToken}'/>">
                <input type="hidden" name="id" value="${b.id}">
                <button type="submit" class="btn-delete" title="<fmt:message key='book.delete'/>" aria-label="<fmt:message key='book.delete'/>">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <polyline points="3 6 5 6 21 6"></polyline>
                    <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path>
                    <line x1="10" y1="11" x2="10" y2="17"></line>
                    <line x1="14" y1="11" x2="14" y2="17"></line>
                  </svg>
                </button>
              </form>
            </div>

            <div class="book-card__meta">
              <a class="book-card__title" href="${ctx}/read?id=${b.id}" title="<c:out value='${b.title}'/>">
                <c:out value="${b.title}"/>
              </a>
              <div class="book-card__details">
                <span class="book-card__pages">
                  <fmt:message key="reader.page"/> ${b.lastPage} / ${b.pageCount}
                </span>
                <c:if test="${b.progressPercent > 0}">
                  <span class="book-card__pct">${b.progressPercent}%</span>
                </c:if>
              </div>
            </div>
          </article>
        </c:forEach>
      </div>
    </c:otherwise>
  </c:choose>
</section>

<script src="${ctx}/static/js/library.js" defer></script>
<%@ include file="_footer.jspf" %>

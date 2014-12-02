package com.sirma.itt.emf.help.history;

/**
 * Interface for manager of history for visited page. * 
 *
 * @author Boyan Tonchev
 */
public interface HistoryManagerInterface {

   /**
	* Initialize history manager.
	*/
	public void init();
	
	/**
	 * Return page after current one. If no more page after current one, the method
	 * will return last one.
	 * @return PageUrl object represent the page after current one.
	 */
	public PageUrl forward();
	
	/**
	 * Return page before current one. If no more page before current one, the method
	 * will return first one.
	 * @return PageUrl object represent the page before current one.
	 */
	public PageUrl back();
	
	/**
	 * Add new page into history. If history manager is in the middle of history stack, page after
	 * current one for manager will be erase and new one will be added as last.
	 * @param pageUrl - page to be added.
	 */
	public void addPageUrl(PageUrl pageUrl);
	
	/**
	 * @return url of current page.
	 */
	public String getCurrentPageContentUrl();
	
	/**
	 * @return navigator's state of current page.
	 */
	public String getCurentPageNavigatorState();
	
	/**
	 * 
	 * @return true if have more elements.
	 */
	public Boolean hasMoreBack();
	
	/**
	 * 
	 * @return true if have more elements.
	 */
	public Boolean hasMoreForward();
}

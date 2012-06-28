function MultiSelector( list_target, list_name, max ){
	
	this.list_target = list_target;	
	this.count = 0;	
	this.list_name = list_name;	
	this.id = 0;
	
	// Larger than max to provide a "buffer" slot for the blank file input field 
	// (at maximum there are 'max' hidden input fields + the blank file input field 
	// that might be used if the user deletes an earlier selection)
	// 
	// Because of this, the controller should be able to handle 1 more file than the user
	// actually can upload.
	this.usedIDs = new Array(max + 1);
		
	// Initiate values		
	for (var i = 0 ; i < this.usedIDs.length ; i++)
	{
	   this.usedIDs[i] = false;
	}
	
	
	if( max ){
		this.max = max;
	} else {
		this.max = -1;
	};
	
	
	/**
	 * Add a new file input element
	 */
	this.addElement = function( element )
	{
		
		if( element.tagName == 'INPUT' && element.type == 'file' ){
				    
			element.name = this.list_name + '_file_' + this.getFreeID();
			element.multi_selector = this;

			element.onchange = function(){

				// New file input
				var new_element = document.createElement( 'input' );
				new_element.type = 'file';		
				this.parentNode.insertBefore( new_element, this );
				this.multi_selector.addElement( new_element );
				this.multi_selector.addListRow( this );

				this.style.position = 'absolute';
				this.style.left = '-1000px';		

			};
			if( this.max != -1 && this.count >= this.max ){
				element.disabled = true;
			};
			
			this.count++;
			this.current_element = element;
			
		} else {
			alert( 'Error: not a file input element' );
		};

	};

	/**
	 * Add a new row to the list of files
	 */
	this.addListRow = function( element )
	{

		var new_row = document.createElement( 'div' );
		
		var new_row_button = document.createElement( 'input' );
		new_row_button.type = 'button';
		new_row_button.value = 'Delete';
		
		new_row.element = element;		
		
		// Delete button
		new_row_button.onclick= function()
		{
			this.parentNode.element.parentNode.removeChild( this.parentNode.element );
			this.parentNode.parentNode.removeChild( this.parentNode );
			this.parentNode.element.multi_selector.count--;
			this.parentNode.element.multi_selector.current_element.disabled = false;
			
			// This line frees up the ID of the hidden file input that was deleted.
			// This implementation only works for indices up to 9. TODO: general solution
			this.parentNode.element.multi_selector.usedIDs
			  [
			    this.parentNode.element.name.charAt
			      (this.parentNode.element.name.length - 1) - 1
			  ] = false;

			return false;
		};
		
		new_row.innerHTML = element.value;

		new_row.appendChild( new_row_button );

		this.list_target.appendChild( new_row );
		
	};
	
	/**
	 * Finds a free ID number
	 */	
	this.getFreeID = function()
	{
	   for (var j = 0 ; j < this.usedIDs.length ; j++)
	   {
	    	if (!this.usedIDs[j])
	    	{
	    	   this.usedIDs[j] = true;
	    	   return (j + 1); // Eternal 0-vs-1-index-start-fight continues
	    	}  
	   }
	 
	}	
};
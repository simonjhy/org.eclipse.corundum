/******************************************************************************
 * Copyright (c) 2016 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.corundum;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author <a href="konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class ManifestBundlesListEntry
{
    private String bundleId;
    private final List<String> attributes = new CopyOnWriteArrayList<String>();
    
    public ManifestBundlesListEntry( String bundleId,
                                     final List<String> attributes )
    {
        this.bundleId = bundleId;
        this.attributes.addAll( attributes );
    }
    
    public String bundle()
    {
        return this.bundleId;
    }
    
    public List<String> attributes()
    {
        return this.attributes;
    }
    
    public String toString()
    {
        final StringBuilder buf = new StringBuilder();
        
        buf.append( this.bundleId );
        
        for( String attribute : attributes )
        {
            buf.append( ';' );
            buf.append( attribute );
        }
        
        return buf.toString();
    }
    
    public static List<ManifestBundlesListEntry> parse( final String string )
    {
        final List<ManifestBundlesListEntry> entries = new ArrayList<ManifestBundlesListEntry>();
        final int length = string.length();
        final MutableReference<Integer> position = new MutableReference<Integer>( 0 );
        
        while( position.get() < length )
        {
            final ManifestBundlesListEntry entry = parse( string, position );
            
            if( entry != null )
            {
                entries.add( entry );
            }
        }
        
        return entries;
    }
    
    private static ManifestBundlesListEntry parse( final String string,
                                                   final MutableReference<Integer> position )
    {
        int stopChar;
        
        final StringBuilder bundleIdBuffer = new StringBuilder();
        stopChar = readUntil( string, position, bundleIdBuffer, ";," );
        
        if( stopChar == -1 )
        {
            return null;
        }
        
        final List<String> attributes = new ArrayList<String>();
        
        while( stopChar == ';' )
        {
            final StringBuilder attribute = new StringBuilder();
            stopChar = readUntil( string, position, attribute, ";," );
            attributes.add( attribute.toString() );
        }
        
        final String bundleId = bundleIdBuffer.toString().trim();
        
        if( bundleId.length() > 0 )
        {
            return new ManifestBundlesListEntry( bundleId, attributes );
        }
        else
        {
            return null;
        }
    }
    
    private static int readUntil( final String string,
                                  final MutableReference<Integer> position,
                                  final StringBuilder buffer,
                                  final String stopChars )
    {
        int pos = position.get();
        int ch = -1;
        boolean stop = false;
        
        for( int len = string.length(); pos < len && ! stop; pos++ )
        {
            ch = string.charAt( pos );
            
            if( stopChars.indexOf( ch ) != -1 )
            {
                stop = true;
            }
            else
            {
                buffer.append( (char) ch );
                
                if( ch == '"' )
                {
                    position.set( pos + 1 );
                    
                    if( readUntil( string, position, buffer, "\"" ) != -1 )
                    {
                        buffer.append( '"' );
                    }
                    
                    pos = position.get() - 1;
                }
            }
        }
        
        position.set( pos );
        
        return ch;
    }
}

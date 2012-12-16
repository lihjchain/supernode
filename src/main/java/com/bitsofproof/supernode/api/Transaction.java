/*
 * Copyright 2012 Tamas Blummer tamas@bitsofproof.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.bitsofproof.supernode.api;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public class Transaction implements Serializable, Cloneable
{
	private static final long serialVersionUID = 690918485496086537L;

	private long version = 1;

	private long lockTime = 0;
	private String hash;
	private String blockHash;

	private List<TransactionInput> inputs;
	private List<TransactionOutput> outputs;

	public long getVersion ()
	{
		return version;
	}

	public String getBlockHash ()
	{
		return blockHash;
	}

	public void setBlockHash (String blockHash)
	{
		this.blockHash = blockHash;
	}

	public void setVersion (long version)
	{
		this.version = version;
	}

	public long getLockTime ()
	{
		return lockTime;
	}

	public void setLockTime (long lockTime)
	{
		this.lockTime = lockTime;
	}

	public void computeHash ()
	{
		WireFormat.Writer writer = new WireFormat.Writer ();
		toWire (writer);
		WireFormat.Reader reader = new WireFormat.Reader (writer.toByteArray ());
		hash = reader.hash ().toString ();
		if ( inputs != null )
		{
			for ( TransactionInput in : inputs )
			{
				in.setTransactionHash (hash);
			}
		}
		if ( outputs != null )
		{
			for ( TransactionOutput out : outputs )
			{
				out.setTransactionHash (hash);
			}
		}
	}

	public String getHash ()
	{
		return hash;
	}

	public void setHash (String hash)
	{
		this.hash = hash;
	}

	public List<TransactionInput> getInputs ()
	{
		return inputs;
	}

	public void setInputs (List<TransactionInput> inputs)
	{
		this.inputs = inputs;
	}

	public List<TransactionOutput> getOutputs ()
	{
		return outputs;
	}

	public void setOutputs (List<TransactionOutput> outputs)
	{
		this.outputs = outputs;
	}

	public void toWire (WireFormat.Writer writer)
	{
		writer.writeUint32 (version);
		if ( inputs != null )
		{
			writer.writeVarInt (inputs.size ());
			for ( TransactionInput input : inputs )
			{
				input.toWire (writer);
			}
		}
		else
		{
			writer.writeVarInt (0);
		}

		if ( outputs != null )
		{
			writer.writeVarInt (outputs.size ());
			for ( TransactionOutput output : outputs )
			{
				output.toWire (writer);
			}
		}
		else
		{
			writer.writeVarInt (0);
		}

		writer.writeUint32 (lockTime);
	}

	public static Transaction fromWire (WireFormat.Reader reader)
	{
		Transaction t = new Transaction ();

		int cursor = reader.getCursor ();

		t.version = reader.readUint32 ();
		long nin = reader.readVarInt ();
		if ( nin > 0 )
		{
			t.inputs = new ArrayList<TransactionInput> ();
			for ( int i = 0; i < nin; ++i )
			{
				t.inputs.add (TransactionInput.fromWire (reader));
			}
		}
		else
		{
			t.inputs = null;
		}

		long nout = reader.readVarInt ();
		if ( nout > 0 )
		{
			t.outputs = new ArrayList<TransactionOutput> ();
			for ( long i = 0; i < nout; ++i )
			{
				t.outputs.add (TransactionOutput.fromWire (reader));
			}
		}
		else
		{
			t.outputs = null;
		}

		t.lockTime = reader.readUint32 ();

		t.hash = reader.hash (cursor, reader.getCursor () - cursor).toString ();

		if ( t.inputs != null )
		{
			for ( TransactionInput in : t.inputs )
			{
				in.setTransactionHash (t.hash);
			}
		}
		if ( t.outputs != null )
		{
			for ( TransactionOutput out : t.outputs )
			{
				out.setTransactionHash (t.hash);
			}
		}
		return t;
	}

	@Override
	public Transaction clone ()
	{
		Transaction t = new Transaction ();

		t.version = version;
		if ( inputs != null )
		{
			t.inputs = new ArrayList<TransactionInput> (inputs.size ());
			for ( TransactionInput i : inputs )
			{
				t.inputs.add (i.clone ());
			}
		}
		if ( outputs != null )
		{
			t.outputs = new ArrayList<TransactionOutput> (outputs.size ());
			for ( TransactionOutput o : outputs )
			{
				t.outputs.add (o.clone ());
			}
		}

		t.lockTime = lockTime;

		t.hash = hash;

		t.blockHash = blockHash;

		return t;
	}
}
